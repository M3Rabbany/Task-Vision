package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest taskRequest);
    TaskResponse updateTask(String taskId, TaskRequest taskRequest);
    void deleteTask(String taskId);
    List<TaskResponse> getTasksByProject(String projectId);
    List<TaskResponse> getTasksByAssignedUser(String assignedToId);
    List<TaskResponse> getTasksByStatus(String status);
}
