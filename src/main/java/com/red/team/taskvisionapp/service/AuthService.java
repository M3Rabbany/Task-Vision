package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(AuthRequest request);
}
