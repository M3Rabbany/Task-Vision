package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.request.ForgotPasswordRequest;
import com.red.team.taskvisionapp.model.dto.request.ResetPasswordRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;
import com.red.team.taskvisionapp.service.AuthService;
import com.red.team.taskvisionapp.service.JwtService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;


    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponse>> login(AuthRequest request) {
        LoginResponse login = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.<LoginResponse>builder()
                        .data(login)
                        .statusCode(HttpStatus.OK.value())
                        .message("Login successfully!")
                        .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.<Void>builder()
                        .message("Password reset email sent successfully!")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<CommonResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Received Token: " + request.getToken());  // Log to check token
        try {
            String email = jwtService.validateAndExtractEmail(request.getToken());
            authService.resetPassword(request);
            return ResponseEntity.ok(CommonResponse.<String>builder()
                    .message("Password reset link sent successfully!")
                    .data("Reset link sent to " + email)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.<String>builder()
                    .message("Invalid token or user not found")
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .build());
        }
    }

}
