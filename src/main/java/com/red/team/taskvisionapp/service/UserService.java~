package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.UserAccount;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse createUser(UserRequest userRequest);
    UserAccount getUserByEmail(String email);
    UserResponse updateUser(String id, UpdateUserRequest request);
    void deleteUser(String id);
}
