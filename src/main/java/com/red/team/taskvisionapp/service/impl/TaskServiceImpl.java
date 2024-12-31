package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.model.dto.request.*;
import com.red.team.taskvisionapp.model.dto.response.FeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.entity.*;
import com.red.team.taskvisionapp.repository.*;
import com.red.team.taskvisionapp.service.TaskService;
import com.red.team.taskvisionapp.service.ValidationService;
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


    @Override
    public List<TaskResponse> getTasksByProject(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    @Override
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

        task.setProject(project);
        task.setAssignedTo(user);
        taskRepository.save(task);

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been assigned to " + user.getName() + ".")
                .type(TypeNotification.INFO)
                .isRead(false)
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

        if (task.getFeedback() != null && !task.getFeedback().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot approve a task with feedback provided");
        }

        task.setStatus(TaskStatus.APPROVED);
        task.setProject(project);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been approved.")
                .type(TypeNotification.INFO)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(task.getAssignedTo())
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);
    }

    @Override
    public FeedbackResponse feedbackTask(TaskFeedbackRequest request) {
        validationService.validate(request);

        if (isCurrentUserPM()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Project Managers can provide feedback");
        }

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

        Feedback feedback = Feedback.builder()
                .task(task)
                .title(request.getFeedback())
                .feedback(request.getFeedback())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(feedback);

        if (task.getFeedback() != null && !task.getFeedback().isEmpty()) {
            task.setStatus(TaskStatus.REJECTED);
        }

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been rejected.")
                .type(TypeNotification.WARNING)
                .isRead(false)
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

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been created.")
                .type(TypeNotification.INFO)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(user)
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);

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

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been updated.")
                .type(TypeNotification.INFO)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(user)
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);

        return toTaskResponse(task);
    }

    @Override
    public void deleteTask(String taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    private boolean isCurrentUserPM() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PROJECT_MANAGER"));
    }

    private TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .taskName(task.getTaskName())
                .deadline(task.getDeadline())
                .status(TaskStatus.valueOf(task.getStatus().toString()))
                .feedback(task.getFeedback())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .taskId(feedback.getTask().getId())
                .title(feedback.getTask().getTaskName())
                .feedback(feedback.getFeedback())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
