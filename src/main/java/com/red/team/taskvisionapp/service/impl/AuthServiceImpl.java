package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;
import com.red.team.taskvisionapp.model.entity.UserAccount;
import com.red.team.taskvisionapp.service.AuthService;
import com.red.team.taskvisionapp.service.JwtService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )

            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserAccount user = (UserAccount) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            return LoginResponse.builder()
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .token(token)
                    .build();
        }catch (BadCredentialsException e){
            throw new ValidationException(("Username or Password is incorrect"));
        }
    }
}
