package com.red.team.taskvisionapp.service.impl;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.red.team.taskvisionapp.constant.ProjectStatus;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.ProjectAssignRequest;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.request.UpdateProjectStatusRequest;
import com.red.team.taskvisionapp.model.dto.response.ProjectResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.ProjectService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;

    @Override
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponse> getAllProjectsByUserId(String userId) {
        List<Project> projects = projectRepository.findAllByUsers_Id(userId);
        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        Project project = mapToEntity(projectRequest);
        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(String id, ProjectRequest projectRequest) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setProjectName(projectRequest.getProjectName());
        project.setDescription(projectRequest.getDescription());
        project.setStatus(projectRequest.getStatus());
        project.setDeadline(projectRequest.getDeadline());
        project = projectRepository.save(project);
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
    public void assignUserToProject(ProjectAssignRequest request) {
        validationService.validate(request);
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        List<User> users = userRepository.findAllById(request.getUserId());
        users.forEach(user -> {
            if(project.getUsers().contains(user)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already assigned to project");
            }
            if(user.getRole() == UserRole.ADMIN){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin cannot be assigned to project");
            }
        });
        project.setUsers(users);
        projectRepository.save(project);
    }

    @Override
    public ProjectResponse updateStatus(UpdateProjectStatusRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setStatus(request.getProjectStatus());
        projectRepository.save(project);
        return mapToResponse(project);
    }


    private Project mapToEntity(ProjectRequest projectRequest) {
        Project project = new Project();
        project.setProjectName(projectRequest.getProjectName());
        project.setDescription(projectRequest.getDescription());
        project.setStatus(projectRequest.getStatus());
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
        response.setUpdatedAt(project.getUpdatedAt());
        response.setUsers(project.getUsers().stream().map(user -> {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setName(user.getName());
            userResponse.setEmail(user.getEmail());
            userResponse.setRole(user.getRole().name());
            userResponse.setContact(user.getContact());
            userResponse.setKpi(user.getKpi());
            userResponse.setCreatedAt(user.getCreatedAt());
            userResponse.setUpdatedAt(user.getUpdatedAt());
            return userResponse;
        }).collect(Collectors.toList()));
        return response;
    }

    @Override
    public List<ProjectResponse> getAllProjectStatuses() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


}
