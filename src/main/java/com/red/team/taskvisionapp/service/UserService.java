package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskWithFeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    List<UserResponse> getAllUsers();
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(String id, UpdateUserRequest request);
    void deleteUser(String id);
    List<TaskResponse> getAllTasksByUserId(String id);
    List<TaskResponse> getPendingTaskById(String id);
    List<TaskWithFeedbackResponse> getTasksWithFeedback(String userId);
    TaskResponse requestApprovalTask(String taskId);
}
