package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskWithFeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.Feedback;
import com.red.team.taskvisionapp.model.entity.Notification;
import com.red.team.taskvisionapp.model.entity.NotificationMember;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.model.entity.Task;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.FeedbackRepository;
import com.red.team.taskvisionapp.repository.NotificationMemberRepository;
import com.red.team.taskvisionapp.repository.NotificationRepository;
import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.repository.TaskRepository;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.UserService;
import com.red.team.taskvisionapp.service.ValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    private final ProjectRepository projectRepository;
    private final EmailServiceImpl emailService;


    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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

        emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().name()
        );

        notifyUser(
                savedUser,
                "Halo, " + savedUser.getName() + "! Your account has been successfully created.",
                TypeNotification.INFO);

        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        validationService.validate(request);

        User account = userRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!account.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        if (!Objects.equals(account.getContact(), request.getContact())
                && userRepository.existsByContact(request.getContact())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact already exists");
        }

        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setRole(UserRole.valueOf(request.getRole()));
        account.setContact(request.getContact());
        account.setKpi(request.getKpi());
        if (StringUtils.hasText(request.getPassword())) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        account.setUpdatedAt(LocalDateTime.now());
        userRepository.save(account);

        notifyUser(
                account,
                "Halo, " + account.getName() + "! Your account has been successfully updated.",
                TypeNotification.INFO);

        return convertToResponse(account);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User account = userRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User currentUser = getCurrentUser();
        if (currentUser.getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        }

        // Notification membership memakai foreign key ke user. Hapus membership lebih dulu,
        // lalu bersihkan notification yang sudah tidak dimiliki user lain.
        List<NotificationMember> memberships = notificationMemberRepository.findByUser(account);
        List<String> notificationIds = memberships.stream()
                .map(member -> member.getNotification().getId())
                .distinct()
                .collect(Collectors.toList());
        notificationMemberRepository.deleteAll(memberships);
        notificationIds.forEach(notificationId -> {
            if (!notificationMemberRepository.existsByNotificationId(notificationId)) {
                notificationRepository.findById(notificationId)
                        .ifPresent(notificationRepository::delete);
            }
        });

        // Feedback menyimpan referensi ke pembuatnya. Hapus feedback yang dibuat user
        // agar riwayat task lain tetap konsisten dan penghapusan tidak gagal karena FK.
        List<Feedback> authoredFeedback = feedbackRepository.findByCreatedById(account.getId());
        if (!authoredFeedback.isEmpty()) {
            feedbackRepository.deleteAll(authoredFeedback);
        }

        // Lepaskan user dari project sebelum task dihapus agar project tidak menyimpan
        // referensi ke task yang sudah dihapus saat proses merge.
        if (account.getProjects() != null) {
            account.getProjects().forEach(project -> {
                Optional.ofNullable(project.getUsers())
                        .ifPresent(users -> users.removeIf(user -> user.getId().equals(account.getId())));
                projectRepository.save(project);
            });
        }

        // Task memiliki foreign key ke user dan feedback memiliki foreign key ke task.
        // Hapus feedback lalu task dengan bulk delete agar tidak bergantung pada cascade.
        feedbackRepository.deleteByTaskAssignedToId(account.getId());
        taskRepository.deleteByAssignedToId(account.getId());

        // Flush seluruh cleanup (task, feedback, notification, join table) sebelum
        // bulk delete user agar tidak ada foreign key yang masih menahan user.
        userRepository.flush();
        userRepository.deleteUserById(account.getId());
    }

    @Override
    public List<TaskResponse> getAllTasksByUserId(String id) {
        validationService.validate(id);
        assertCurrentUser(id);

        List<Task> tasks = taskRepository.findByAssignedToId(id);
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getPendingTaskById(String id) {
        validationService.validate(id);
        assertCurrentUser(id);

        List<Task> tasks = taskRepository.findByAssignedToIdAndStatus(id, TaskStatus.PENDING);
        return tasks.stream().map(this::convertToTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskWithFeedbackResponse> getTasksWithFeedback(String userId) {
        assertCurrentUser(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Task> tasks = taskRepository.findByAssignedToId(user.getId());
        Comparator<Feedback> byCreatedAt = Comparator.comparing(
                Feedback::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()));

        return tasks.stream()
                .map(task -> {
                    List<Feedback> feedbackList = feedbackRepository.findByTaskId(task.getId());
                    Feedback feedback = feedbackList.stream().max(byCreatedAt).orElse(null);
                    return feedback == null ? null : toTaskWithFeedbackResponse(task, feedback);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse requestApprovalTask(String taskId) {
        validationService.validate(taskId);

        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only request approval for your own task");
        }

        if (TaskStatus.PENDING.equals(task.getStatus()) || TaskStatus.APPROVED.equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is already in Pending or Approved status");
        }

        task.setStatus(TaskStatus.PENDING);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        notifyProjectManagers(
                task.getProject(),
                "Task " + task.getTaskName() + " has been requested for approval.",
                TypeNotification.WARNING);

        return convertToTaskResponse(task);
    }

    private void assertCurrentUser(String userId) {
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId) && currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's tasks");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void notifyProjectManagers(Project project, String content, TypeNotification type) {
        Optional.ofNullable(project)
                .map(Project::getUsers)
                .orElseGet(List::of)
                .stream()
                .filter(user -> user.getRole() == UserRole.PROJECT_MANAGER)
                .forEach(user -> notifyUser(user, content, type));
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
