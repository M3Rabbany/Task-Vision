package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.model.entity.Task;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.TaskRepository;
import com.red.team.taskvisionapp.service.TaskService;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<TaskResponse> getTasksByProject(UUID projectId) {
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
    public List<TaskResponse> getTasksByAssignedUser(UUID assignedToId) {
        return List.of();
    }

    @Override
    public List<TaskResponse> getTasksByStatus(String status) {
        return List.of();
    }

    @Override
    public TaskResponse createTask(TaskRequest taskRequest) {
        Task task = new Task();
        task.setProject(new Project(taskRequest.getProjectId()));
        task.setAssignedTo(new User(taskRequest.getAssignedTo()));
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
    public TaskResponse updateTask(UUID taskId, TaskRequest taskRequest) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));

        task.setProject(new Project(taskRequest.getProjectId()));
        task.setAssignedTo(new User(taskRequest.getAssignedTo()));
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
    public void deleteTask(UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new OpenApiResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }
}
