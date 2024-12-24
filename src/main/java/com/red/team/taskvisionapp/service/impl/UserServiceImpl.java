package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.UserAccount;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.UserService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        validationService.validate(request);

        UserAccount user = UserAccount.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.valueOf(request.getRole()))
                .contact(request.getContact())
                .kpi(request.getKpi())
                .createdAt(LocalDateTime.now())
                .build();
        return convertToResponse(userRepository.save(user));
    }

    @Override
    public UserAccount getUserByEmail(String email) {
        return userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));
    }

    @Override
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        validationService.validate(request);

        UserAccount account = userRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));

        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setRole(UserRole.valueOf(request.getRole()));
        account.setContact(request.getContact());
        account.setKpi(request.getKpi());
        account.setUpdatedAt(LocalDateTime.now());
        userRepository.save(account);

        return convertToResponse(account);
    }

    @Override
    public void deleteUser(String id) {
        UserAccount account = userRepository.findFirstById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ("User not found")));
        userRepository.delete(account);
    }

    private UserResponse convertToResponse(UserAccount user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .contact(user.getContact())
                .kpi(user.getKpi())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
