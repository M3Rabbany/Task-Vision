package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.*;
import com.red.team.taskvisionapp.model.dto.response.FeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(TaskRequest taskRequest);
    TaskResponse updateTask(String taskId, TaskRequest taskRequest);
    void deleteTask(String taskId);
    List<TaskResponse> getTasksByProject(String projectId);
    void assignTaskToMember(TaskAssignRequest request);
    void approveTask(TaskApproveRequest request);
    FeedbackResponse feedbackTask(TaskFeedbackRequest request);
}
