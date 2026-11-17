package com.red.team.taskvisionapp.service.impl;
import com.red.team.taskvisionapp.constant.ProjectStatus;
import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.ProjectAssignRequest;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.request.UpdateProjectStatusRequest;
import com.red.team.taskvisionapp.model.dto.response.ProjectResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.*;
import com.red.team.taskvisionapp.repository.*;
import com.red.team.taskvisionapp.service.ActivityService;
import com.red.team.taskvisionapp.service.ProjectService;
import com.red.team.taskvisionapp.service.ValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final NotificationRepository notificationRepository;
    private final NotificationMemberRepository notificationMemberRepository;
    private final EmailServiceImpl emailService;
    private final ActivityService activityService;


    @Override
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getAllProjectsByUserId(String userId) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN && !currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's projects");
        }

        List<Project> projects = projectRepository.findAllByUsers_Id(userId);
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        Project project = mapToEntity(projectRequest);
        project = projectRepository.save(project);
        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Project");
        activity.setAction("Created");
        activity.setDetails("Initialized Project");
        activityService.createActivity(activity);
        return mapToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(String id, ProjectRequest projectRequest) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setProjectName(projectRequest.getProjectName());
        project.setDescription(projectRequest.getDescription());
        project.setDeadline(projectRequest.getDeadline());
        project = projectRepository.save(project);
        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Project");
        activity.setAction("Updated");
        activity.setDetails("Updated Description Project");
        activityService.createActivity(activity);
        return mapToResponse(project);
    }

    @Override
    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found");
        }
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void assignUserToProject(ProjectAssignRequest request) {
        validationService.validate(request);
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        List<User> users = userRepository.findAllById(request.getUserId());
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one user must be selected");
        }
        if (users.size() != request.getUserId().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more selected users were not found");
        }
        users.forEach(user -> {
            if (user.getRole() == UserRole.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin cannot be assigned to project");
            }
        });

        // Sinkronkan anggota tim project dengan pilihan terbaru dari frontend.
        // User yang sudah terpilih tidak lagi dianggap sebagai error agar form
        // assign dapat dipakai berulang kali untuk menambah/mengurangi anggota.
        List<User> currentUsers = Optional.ofNullable(project.getUsers())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
        List<User> newUsers = users.stream()
                .filter(user -> !currentUsers.contains(user))
                .collect(Collectors.toList());

        project.setUsers(users);
        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Project");
        activity.setAction("Updated");
        activity.setDetails("Assign User to Project");
        activityService.createActivity(activity);
        projectRepository.save(project);

        newUsers.forEach(user -> {
            String emailSubject = "You have been assigned to a project";
            String emailBody = "Hello " + user.getName() + ",\n\n" +
                    "You have been assigned to a project by the project manager.\n" +
                    "Project name: " + project.getProjectName() + "\n\n" +
                    "Best regards,\n" +
                    "TaskVisionApp";
            emailService.sendEmail(user.getEmail(), emailSubject, emailBody);

            Notification notification = Notification.builder()
                    .content("You have been assigned to project " + project.getProjectName() + ".")
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
        });
    }

    @Override
    @Transactional
    public ProjectResponse updateStatus(UpdateProjectStatusRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        assertCanManageProject(project);

        project.setStatus(request.getProjectStatus());
        projectRepository.save(project);
        Activity activity = new Activity();
        activity.setProject(project);
        activity.setEntity("Project");
        activity.setAction("Update");
        activity.setDetails("Update Status Project to " + request.getProjectStatus());
        activityService.createActivity(activity);
        return mapToResponse(project);
    }

    private void assertCanManageProject(Project project) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        boolean assigned = Optional.ofNullable(project.getUsers())
                .orElseGet(Collections::emptyList)
                .stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));
        if (currentUser.getRole() != UserRole.PROJECT_MANAGER || !assigned) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this project");
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



    private Project mapToEntity(ProjectRequest projectRequest) {
        Project project = new Project();
        project.setProjectName(projectRequest.getProjectName());
        project.setDescription(projectRequest.getDescription());
        project.setStatus(ProjectStatus.IN_PROGRESS);
        project.setDeadline(projectRequest.getDeadline());
        return project;
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectName(project.getProjectName());
        response.setDescription(project.getDescription());
        response.setStatus(project.getStatus());
        response.setDeadline(project.getDeadline());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt() != null ? project.getUpdatedAt() : project.getCreatedAt());

        List<UserResponse> userResponses = Optional.ofNullable(project.getUsers())
                .orElse(Collections.emptyList())
                .stream()
                .map(user -> {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setId(user.getId());
                    userResponse.setName(user.getName());
                    userResponse.setEmail(user.getEmail());
                    userResponse.setContact(user.getContact());
                    userResponse.setKpi(user.getKpi());
                    userResponse.setRole(user.getRole());
                    userResponse.setCreatedAt(user.getCreatedAt());
                    userResponse.setUpdatedAt(user.getUpdatedAt());
                    return userResponse;
                })
                .collect(Collectors.toList());
        response.setUsers(userResponses);
        return response;
    }

}
