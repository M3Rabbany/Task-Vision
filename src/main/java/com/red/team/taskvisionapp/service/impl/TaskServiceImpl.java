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
import java.util.ArrayList;
import java.util.List;
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
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignTaskToMember(TaskAssignRequest request) {
        validationService.validate(request);

        if (isCurrentUserPM()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Project Managers can assign tasks");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Project not found")));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));

        if (!task.getProject().getId().equals(project.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not belong to the specified project");
        }

        if (task.getStatus().equals(TaskStatus.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task already approve and cannot be reassigned");
        }

        if (!user.getRole().equals(UserRole.MEMBER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Task can only be assigned to members");
        }

        task.setProject(project);
        task.setAssignedTo(user);
        taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Assign User to Task");
        activityService.createActivity(activity);

        String emailSubject = "Task assigned to you";
        String emailBody = "Hello " + user.getName() + ",\n\n" +
                "Your task " + task.getTaskName() + " has been assigned to you.\n\n" +
                "Please complete the task and submit before deadline.\n\n" +
                "Best regards,\n" +
                "TaskVisionApp";

        emailService.sendEmail(user.getEmail(), emailSubject, emailBody);

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been assigned to " + user.getName() + ".")
                .type(TypeNotification.INFO)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(user)
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);
    }

    @Override
    public void approveTask(TaskApproveRequest request) {
        validationService.validate(request);

        if (isCurrentUserPM()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Project Managers can approve tasks");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Project not found")));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

        if (task.getStatus() == TaskStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot provide feedback to an approved task");
        }

        task.setStatus(TaskStatus.APPROVED);
        task.setProject(project);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Approve Task");
        activityService.createActivity(activity);

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been approved.")
                .type(TypeNotification.INFO)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(task.getAssignedTo())
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);
    }

    @Override
    @Transactional
    public FeedbackResponse feedbackTask(TaskFeedbackRequest request) {
        validationService.validate(request);

        if (isCurrentUserPM()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Project Managers can provide feedback");
        }

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

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
        taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(task.getProject());
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Reject task and give feedback");
        activityService.createActivity(activity);

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been rejected.")
                .type(TypeNotification.WARNING)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(task.getAssignedTo())
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);

        return toFeedbackResponse(feedback);
    }


    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        Project project = projectRepository.findById(taskRequest.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Project not found")));
        User user = userRepository.findById(taskRequest.getAssignedTo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));
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

        return toTaskResponse(task);
    }

    @Override
    public TaskResponse updateTask(String taskId, TaskRequest taskRequest) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));
        Project project = projectRepository.findById(taskRequest.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Project not found")));
        User user = userRepository.findById(taskRequest.getAssignedTo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));
        task.setProject(project);
        task.setAssignedTo(user);
        task.setTaskName(taskRequest.getTaskName());
        task.setDeadline(taskRequest.getDeadline());
        task.setStatus(TaskStatus.ON_GOING);
        task.setUpdatedAt(LocalDateTime.now());

        task = taskRepository.save(task);

        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Update task description");
        activityService.createActivity(activity);

        return toTaskResponse(task);
    }

    @Override
    public void deleteTask(String taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));

        Activity activity = new Activity();
        activity.setProject(task.getProject());
        activity.setEntity("Task");
        activity.setAction("Update");
        activity.setDetails("Delete task: " + task.getTaskName());
        activityService.createActivity(activity);

        taskRepository.delete(task);
    }

    private boolean isCurrentUserPM() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PROJECT_MANAGER"));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Current User Name/Email: " + email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
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
                        .taskName(feedback.getTask().getTaskName())
                        .deadline(feedback.getTask().getDeadline())
                        .status(TaskStatus.valueOf(feedback.getTask().getStatus().toString()))
                        .createdAt(feedback.getTask().getCreatedAt())
                        .updatedAt(feedback.getTask().getUpdatedAt())
                        .build())
                .title(feedback.getTitle())
                .feedback(feedback.getFeedback())
                .createdBy(feedback.getCreatedBy().getName())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();

    }
}
