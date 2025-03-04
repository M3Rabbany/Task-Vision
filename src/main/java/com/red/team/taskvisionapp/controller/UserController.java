package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.*;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.USER)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/get")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        System.out.println("getAllUsers");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<UserResponse>> createUser(@RequestBody @Valid UserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.ok(CommonResponse.<UserResponse>builder()
                .message("User created successfully!")
                .data(user)
                .statusCode(HttpStatus.CREATED.value())
                .build());
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<CommonResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .message("User deleted successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }
    @GetMapping("/{userId}/tasks")
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getAllTasksByUserId(@PathVariable String userId) {
        List<TaskResponse> tasks = userService.getAllTasksByUserId(userId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{userId}/tasks/pending")
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getPendingTasksByUserId(@PathVariable String userId) {
        List<TaskResponse> tasks = userService.getPendingTaskById(userId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{userId}/tasks/feedback")
    public ResponseEntity<CommonResponse<List<TaskWithFeedbackResponse>>> getTasksWithFeedback(@PathVariable String userId) {
        List<TaskWithFeedbackResponse> tasksWithFeedback = userService.getTasksWithFeedback(userId);
        return ResponseEntity.ok(CommonResponse.<List<TaskWithFeedbackResponse>>builder()
                .message("Tasks with feedback retrieved successfully!")
                .data(tasksWithFeedback)
                .statusCode(HttpStatus.OK.value())
                .build());
    }


    @PutMapping("{taskId}/approval")
    public ResponseEntity<CommonResponse<String>> approveTask(@PathVariable String taskId) {
        TaskResponse taskResponse = userService.requestApprovalTask(taskId);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task request approved successfully!")
                .statusCode(HttpStatus.OK.value())
                .data(taskResponse.getStatus().toString())
                .build());
    }
}

