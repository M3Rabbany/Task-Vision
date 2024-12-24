package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.response.UsersResponse;

import java.util.List;

public interface UsersService {
    UsersResponse getUserById(String id);
    List<UsersResponse> getAllUsersByName(String name);
}
