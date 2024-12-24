package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.UserAccount;
import com.red.team.taskvisionapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CommonResponse<UserAccount>> getUserByEmail(@PathVariable String email) {
        UserAccount user = userService.getUserByEmail(email);
        return ResponseEntity.ok(CommonResponse.<UserAccount>builder()
                        .message("User found!")
                        .data(user)
                        .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponse>> createUser(@RequestBody @Valid UserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(CommonResponse.<UserResponse>builder()
                .message("User created successfully!")
                .data(user)
                .statusCode(HttpStatus.CREATED.value())
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponse>> updateUser(
            @PathVariable String id,
            @RequestBody @Valid UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(CommonResponse.<UserResponse>builder()
                .message("User updated successfully!")
                .data(user)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .message("User deleted successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

}

