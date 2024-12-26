package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse createUser(UserRequest userRequest);
    User getUserByEmail(String email);
    UserResponse updateUser(String id, UpdateUserRequest request);
    void deleteUser(String id);
}
