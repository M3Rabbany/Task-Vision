package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.*;
import com.red.team.taskvisionapp.model.dto.response.FeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.entity.*;
import com.red.team.taskvisionapp.repository.*;
import com.red.team.taskvisionapp.service.ActivityService;
import com.red.team.taskvisionapp.service.EmailService;
import com.red.team.taskvisionapp.service.TaskService;
import com.red.team.taskvisionapp.service.ValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ValidationService validationService;
    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMemberRepository notificationMemberRepository;
    private final EmailService emailService;
    private final ActivityService activityService;

    @Override
    public List<TaskResponse> getTasksByProject(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        assertCanAccessProject(project);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignTaskToMember(TaskAssignRequest request) {
        validationService.validate(request);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertCanAccessProject(project);
        assertTaskBelongsToProject(task, project);

        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task already approved and cannot be reassigned");
        }

        validateTaskAssignee(project, user);

        task.setAssignedTo(user);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Assign User to Task");
        activityService.createActivity(activity);

        notifyTaskAssigned(task, user);
    }

    @Override
    @Transactional
    public void approveTask(TaskApproveRequest request) {
        validationService.validate(request);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertCanAccessProject(project);
        assertTaskBelongsToProject(task, project);

        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is already approved");
        }
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only tasks waiting for approval can be approved");
        }

        task.setStatus(TaskStatus.APPROVED);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Approve Task");
        activityService.createActivity(activity);

        notifyUser(
                task.getAssignedTo(),
                "Task " + task.getTaskName() + " has been approved.",
                TypeNotification.INFO);
    }

    @Override
    @Transactional
    public FeedbackResponse feedbackTask(TaskFeedbackRequest request) {
        validationService.validate(request);

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        assertCanAccessProject(project);
        assertTaskBelongsToProject(task, project);

        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot provide feedback to an approved task");
        }

        User currentUser = getCurrentUser();
        Feedback feedback = Feedback.builder()
                .task(task)
                .createdBy(currentUser)
                .title(request.getTitle())
                .feedback(request.getFeedback())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(feedback);

        task.setStatus(TaskStatus.REJECTED);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(task.getProject());
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Reject task and give feedback");
        activityService.createActivity(activity);

        notifyUser(
                task.getAssignedTo(),
                "Task " + task.getTaskName() + " has been rejected.",
                TypeNotification.WARNING);

        return toFeedbackResponse(feedback);
    }

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {
        Project project = projectRepository.findById(taskRequest.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User user = userRepository.findById(taskRequest.getAssignedTo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertCanAccessProject(project);
        validateTaskAssignee(project, user);

        Task task = new Task();
        task.setProject(project);
        task.setAssignedTo(user);
        task.setTaskName(taskRequest.getTaskName());
        task.setDeadline(taskRequest.getDeadline());
        task.setStatus(TaskStatus.ON_GOING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Created");
        activity.setDetails("Task Created");
        activityService.createActivity(activity);

        notifyTaskAssigned(task, user);
        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(String taskId, TaskRequest taskRequest) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));
        Project project = projectRepository.findById(taskRequest.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User user = userRepository.findById(taskRequest.getAssignedTo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertCanAccessProject(project);
        assertTaskBelongsToProject(task, project);

        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved tasks cannot be edited");
        }

        validateTaskAssignee(project, user);

        String previousAssigneeId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        task.setAssignedTo(user);
        task.setTaskName(taskRequest.getTaskName());
        task.setDeadline(taskRequest.getDeadline());
        task.setUpdatedAt(LocalDateTime.now());

        task = taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Update task details");
        activityService.createActivity(activity);

        if (previousAssigneeId == null || !previousAssigneeId.equals(user.getId())) {
            notifyTaskAssigned(task, user);
        }

        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));
        assertCanAccessProject(task.getProject());

        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approved tasks cannot be deleted");
        }

        Activity activity = new Activity();
        activity.setProject(task.getProject());
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Delete task: " + task.getTaskName());
        activityService.createActivity(activity);

        taskRepository.delete(task);
    }

    private void assertTaskBelongsToProject(Task task, Project project) {
        if (task.getProject() == null || !task.getProject().getId().equals(project.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not belong to the specified project");
        }
    }

    private void validateTaskAssignee(Project project, User user) {
        if (user.getRole() != UserRole.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Task can only be assigned to members");
        }

        boolean projectMember = Optional.ofNullable(project.getUsers())
                .orElseGet(List::of)
                .stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
        if (!projectMember) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a member of this project");
        }
    }

    private void assertCanAccessProject(Project project) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        boolean allowed = Optional.ofNullable(project.getUsers())
                .orElseGet(List::of)
                .stream()
                .anyMatch(member -> member.getId().equals(currentUser.getId()));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this project");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void notifyTaskAssigned(Task task, User user) {
        if (task == null || user == null) {
            return;
        }

        emailService.sendEmail(
                user.getEmail(),
                "Task assigned to you",
                "Hello " + user.getName() + ",\n\n"
                        + "Your task " + task.getTaskName() + " has been assigned to you.\n\n"
                        + "Please complete the task and submit before deadline.\n\n"
                        + "Best regards,\n"
                        + "TaskVisionApp");

        notifyUser(
                user,
                "Task " + task.getTaskName() + " has been assigned to " + user.getName() + ".",
                TypeNotification.INFO);
    }

    private void notifyUser(User user, String content, TypeNotification type) {
        if (user == null) {
            return;
        }

        Notification notification = Notification.builder()
                .content(content)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        notificationMemberRepository.save(NotificationMember.builder()
                .user(user)
                .notification(savedNotification)
                .build());
    }

    private TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .assignedTo(task.getAssignedTo().getId())
                .taskName(task.getTaskName())
                .deadline(task.getDeadline())
                .status(TaskStatus.valueOf(task.getStatus().toString()))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return getFeedbackResponse(feedback);
    }

    static FeedbackResponse getFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .task(TaskResponse.builder()
                        .id(feedback.getTask().getId())
                        .projectId(feedback.getTask().getProject().getId())
                        .assignedTo(feedback.getTask().getAssignedTo().getId())
                        .taskName(feedback.getTask().getTaskName())
                        .deadline(feedback.getTask().getDeadline())
                        .status(TaskStatus.valueOf(feedback.getTask().getStatus().toString()))
                        .createdAt(feedback.getTask().getCreatedAt())
                        .updatedAt(feedback.getTask().getUpdatedAt())
                        .build())
                .title(feedback.getTitle())
                .feedback(feedback.getFeedback())
                .createdBy(feedback.getCreatedBy().getEmail())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
