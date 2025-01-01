package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.request.ForgotPasswordRequest;
import com.red.team.taskvisionapp.model.dto.request.ResetPasswordRequest;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.AuthService;
import com.red.team.taskvisionapp.service.EmailService;
import com.red.team.taskvisionapp.service.JwtService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )

            );
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return LoginResponse.builder()
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .token(token)
                    .build();
        }catch (BadCredentialsException e){
            throw new ValidationException(("Username or Password is incorrect"));
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("User not found with email: " + request.getEmail()));

        if (!user.getRole().equals(UserRole.ADMIN)) {
            throw new ValidationException("Forgot password is only available for users with ROLE_ADMIN");
        }

        String resetToken = jwtService.generateToken(user);
        String resetLink = "http://localhost:8080/swagger-ui/index.html/reset-password?token=" + resetToken;

        emailService.sendEmail(
                user.getEmail(),
                "Reset Your Password",
                "Click the following link to reset your password: " + resetLink
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }

        String email = jwtService.validateAndExtractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid token or user not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
