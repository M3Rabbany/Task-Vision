package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.request.AuthRequest;
import com.red.team.taskvisionapp.model.dto.response.LoginResponse;
import com.red.team.taskvisionapp.model.entity.User;
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
        System.out.println("Mulai Login");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )

            );
            System.out.println("Context Authentication");
            User user = (User) authentication.getPrincipal();
            System.out.println("principal");
            String token = jwtService.generateToken(user);
            System.out.println("Autheticated");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("decode jwt");
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
