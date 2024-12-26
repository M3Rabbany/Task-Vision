package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest taskRequest);
    TaskResponse updateTask(UUID taskId, TaskRequest taskRequest);
    void deleteTask(UUID taskId);
    List<TaskResponse> getTasksByProject(UUID projectId);
    List<TaskResponse> getTasksByAssignedUser(UUID assignedToId);
    List<TaskResponse> getTasksByStatus(String status);
}
