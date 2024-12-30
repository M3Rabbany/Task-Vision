package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;
import com.red.team.taskvisionapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/login")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<CommonResponse<LoginResponse>> login(AuthRequest request) {
        LoginResponse login = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.<LoginResponse>builder()
                        .data(login)
                        .statusCode(HttpStatus.OK.value())
                        .message("Login successfully!")
                        .build());
    }
}
