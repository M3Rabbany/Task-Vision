package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.*;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest taskRequest);
    TaskResponse updateTask(String taskId, TaskRequest taskRequest);
    void deleteTask(String taskId);
    List<TaskResponse> getTasksByProject(String projectId);
    List<TaskResponse> getAllTasksByUserId(String assignedToId);
    List<TaskResponse> getPendingTaskById(String assignedToId);
    TaskResponse getTaskById(String id, String assignedToId);
    TaskResponse requestApprovalTask(TaskApproveRequest request);
    List<TaskResponse> getTasksByStatus(String status);
    void assignTaskToMember(TaskAssignRequest request);
    void approveTask(TaskApproveRequest request);
    void feedbackTask(TaskFeedbackRequest request);
}
