package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskWithFeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.Notification;
import com.red.team.taskvisionapp.model.entity.NotificationMember;
import com.red.team.taskvisionapp.model.entity.Feedback;
import com.red.team.taskvisionapp.model.entity.Task;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.NotificationMemberRepository;
import com.red.team.taskvisionapp.repository.NotificationRepository;
import com.red.team.taskvisionapp.repository.FeedbackRepository;
import com.red.team.taskvisionapp.repository.TaskRepository;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.UserService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationMemberRepository notificationMemberRepository;
    private final FeedbackRepository feedbackRepository;
    private final EmailServiceImpl emailService;


    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        validationService.validate(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        if (userRepository.existsByContact(request.getContact())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.valueOf(request.getRole().toString()))
                .contact(request.getContact())
                .kpi(request.getKpi())
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        String emailSubject = "Welcome to TaskVision!";
        String emailBody = "Hello, " + savedUser.getName() + "!\nWelcome to TaskVision! Your account has been successfully created.";
        emailService.sendEmail(savedUser.getEmail(), emailSubject, emailBody);

        Notification notification = Notification.builder()
                .content("Halo, " + savedUser.getName() + "! Your account has been successfully created.")
                .type(TypeNotification.INFO)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(savedUser)
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);

        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        validationService.validate(request);

        User account = userRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));

        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setRole(UserRole.valueOf(request.getRole()));
        account.setContact(request.getContact());
        account.setKpi(request.getKpi());
        account.setUpdatedAt(LocalDateTime.now());
        userRepository.save(account);

        Notification notification = Notification.builder()
                .content("Halo, " + account.getName() + "! Your account has been successfully updated.")
                .type(TypeNotification.INFO)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationMember notificationMember = NotificationMember.builder()
                .user(account)
                .notification(savedNotification)
                .build();

        notificationMemberRepository.save(notificationMember);

        return convertToResponse(account);
    }

    @Override
    public void deleteUser(String id) {
        User account = userRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));
        userRepository.delete(account);
    }

    @Override
    public List<TaskResponse> getAllTasksByUserId(String id) {
        validationService.validate(id);

        List<Task> tasks = taskRepository.findByAssignedToId(id);
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getPendingTaskById(String id) {
        validationService.validate(id);

        List<Task> tasks = taskRepository.findByAssignedToIdAndStatus(id, TaskStatus.PENDING); // Pending
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskWithFeedbackResponse> getTasksWithFeedback(String userId) {
        // Mencari user berdasarkan userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Mencari semua task yang diberikan feedback untuk user ini
        List<Task> tasks = taskRepository.findByAssignedToId(user.getId());

        return tasks.stream()
                .map(task -> {
                    List<Feedback> feedbackList = feedbackRepository.findByTaskId(task.getId());

                    Feedback feedback = feedbackList.isEmpty()? Feedback.builder()
                            .task(task)
                            .createdBy(user)
                            .title("No Feedback")
                            .feedback("No feedback available for this task.")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(): feedbackList.get(0);

                    return toTaskWithFeedbackResponse(task, feedback);
                })
                .collect(Collectors.toList());
    }


    @Override
    public TaskResponse requestApprovalTask(String taskId) {
        validationService.validate(taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

        if (TaskStatus.PENDING.equals(task.getStatus()) || TaskStatus.APPROVED.equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is already in Pending or Approved status");
        }

        task.setStatus(TaskStatus.PENDING);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        Notification notification = Notification.builder()
                .content("Task " + task.getTaskName() + " has been requested for approval.")
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

        return convertToTaskResponse(task);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(UserRole.valueOf(user.getRole().toString()))
                .contact(user.getContact())
                .kpi(user.getKpi())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    }

    private TaskResponse convertToTaskResponse(Task task) {
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

    private TaskWithFeedbackResponse toTaskWithFeedbackResponse(Task task, Feedback feedback) {
        return TaskWithFeedbackResponse.builder()
                .taskId(task.getId())
                .feedbackId(feedback.getId())
                .title(feedback.getTitle())
                .feedback(feedback.getFeedback())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

}
