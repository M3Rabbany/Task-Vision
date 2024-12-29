package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.request.*;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.model.entity.Task;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.repository.TaskRepository;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.TaskService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ValidationService validationService;

    @Override
    public List<TaskResponse> getTasksByProject(String projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<TaskResponse> taskResponses = new ArrayList<>();
        for (Task task : tasks) {
            taskResponses.add(new TaskResponse(
                    task.getId(),
                    task.getProject().getId(),
                    task.getAssignedTo().getId(),
                    task.getTaskName(),
                    task.getDeadline(),
                    task.getStatus(),
                    task.getFeedback(),
                    task.getCreatedAt(),
                    task.getUpdatedAt()
            ));
        }
        return taskResponses;
    }

    @Override
    public List<TaskResponse> getAllTasksByUserId(String assignedToId) {
        validationService.validate(assignedToId);

        List<Task> tasks = taskRepository.findByAssignedToId(assignedToId);
        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getPendingTaskById(String assignedToId) {
        validationService.validate(assignedToId);

        List<Task> pendingTasks = taskRepository.findByAssignedToIdAndStatus(assignedToId, "Pending");

        return pendingTasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse getTaskById(String id, String assignedToId) {
        validationService.validate(id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Feedback not found")));

        return toTaskResponse(task);
    }

    @Override
    public TaskResponse requestApprovalTask(TaskApproveRequest request) {
        validationService.validate(request);

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

        task.setStatus("Pending");
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        return toTaskResponse(task);
    }

    @Override
    public List<TaskResponse> getTasksByStatus(String status) {

        if (status == null && status.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }

        List<Task> tasks = taskRepository.findByStatus(status);

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

        task.setStatus("Approved");
        task.setProject(project);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Override
    public void feedbackTask(TaskFeedbackRequest request) {
        validationService.validate(request);

        if (isCurrentUserPM()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Project Managers can provide feedback");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Project not found")));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("Task not found")));

        task.setFeedback(request.getFeedback());
        task.setProject(project);
        task.setStatus("Rejected");
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
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
        task.setStatus(taskRequest.getStatus());
        task.setFeedback(taskRequest.getFeedback());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        task = taskRepository.save(task);

        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getAssignedTo().getId(),
                task.getTaskName(),
                task.getDeadline(),
                task.getStatus(),
                task.getFeedback(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
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
        task.setStatus(taskRequest.getStatus());
        task.setFeedback(taskRequest.getFeedback());
        task.setUpdatedAt(LocalDateTime.now());

        task = taskRepository.save(task);

        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getAssignedTo().getId(),
                task.getTaskName(),
                task.getDeadline(),
                task.getStatus(),
                task.getFeedback(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
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
                .status(task.getStatus())
                .feedback(task.getFeedback())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();

    }
}
