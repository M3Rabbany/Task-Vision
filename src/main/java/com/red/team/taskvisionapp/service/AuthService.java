package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.request.ForgotPasswordRequest;
import com.red.team.taskvisionapp.model.dto.request.ResetPasswordRequest;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(AuthRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);

}
