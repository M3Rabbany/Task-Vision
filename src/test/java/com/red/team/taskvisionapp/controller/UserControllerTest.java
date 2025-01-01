package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.request.UpdateUserRequest;
import com.red.team.taskvisionapp.model.dto.request.UserRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskWithFeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private UserRequest userRequest;
    private UserResponse userResponse;
    private UpdateUserRequest request;
    private User user;
    private TaskResponse taskResponse;
    private List<TaskResponse> taskList;
    private List<TaskWithFeedbackResponse> taskWithFeedbackList;

    @BeforeEach
    void setUp() {
        userRequest = UserRequest.builder()
                .name("Test User")
                .email("test@email.com")
                .password("password123")
                .role(UserRole.ADMIN)
                .contact("1234567890")
                .kpi(4.5f)
                .build();

        userResponse = UserResponse.builder()
                .id("user-id")
                .name("Test User")
                .email("test@email.com")
                .role(UserRole.ADMIN)
                .contact("1234567890")
                .kpi(4.5f)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = User.builder()
                .id("user-id")
                .name("Test User")
                .email("test@email.com")
                .password("hashedPassword")
                .role(UserRole.ADMIN)
                .contact("1234567890")
                .kpi(4.5f)
                .build();

        request = UpdateUserRequest.builder()
                .name("Updated User")
                .email("updated@email.com")
                .role(String.valueOf(UserRole.ADMIN))
                .contact("0987654321")
                .kpi(4.0f)
                .build();

        taskResponse = TaskResponse.builder()
                .id("task-id")
                .taskName("Test Task")
                .projectId("project-id")
                .status(TaskStatus.ON_GOING)
                .assignedTo("user-id")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskWithFeedbackResponse taskWithFeedback = TaskWithFeedbackResponse.builder()
                .taskId("task-id")
                .feedbackId("feedback-id")
                .title("Test Feedback")
                .feedback("Test Feedback")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskList = List.of(taskResponse);
        taskWithFeedbackList = List.of(taskWithFeedback);
    }

    @Test
    void testGetAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        ResponseEntity<List<UserResponse>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(userResponse), response.getBody());
        verify(userService).getAllUsers();
    }

    @Test
    void testGetUserByEmail() {
        when(userService.getUserByEmail("test@email.com")).thenReturn(user);

        ResponseEntity<CommonResponse<User>> response = userController.getUserByEmail("test@email.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User found!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(user, response.getBody().getData());
        verify(userService).getUserByEmail("test@email.com");
    }

    @Test
    void testCreateUser() {
        when(userService.createUser(userRequest)).thenReturn(userResponse);

        ResponseEntity<CommonResponse<UserResponse>> response = userController.createUser(userRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(userResponse, response.getBody().getData());
        verify(userService).createUser(userRequest);
    }

    @Test
    void testUpdateUser() {
        when(userService.updateUser(eq("user-id"), any(UpdateUserRequest.class))).thenReturn(userResponse);

        ResponseEntity<CommonResponse<UserResponse>> response = userController.updateUser("user-id", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(userResponse, response.getBody().getData());
        verify(userService).updateUser(eq("user-id"), any(UpdateUserRequest.class));
    }

    @Test
    void testDeleteUser() {
        willDoNothing().given(userService).deleteUser("user-id");

        ResponseEntity<CommonResponse<Void>> response = userController.deleteUser("user-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        verify(userService).deleteUser("user-id");
    }

    @Test
    void testGetAllTasksByUserId() {
        when(userService.getAllTasksByUserId("user-id")).thenReturn(taskList);

        ResponseEntity<CommonResponse<List<TaskResponse>>> response = userController.getAllTasksByUserId("user-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tasks retrieved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskList, response.getBody().getData());
        verify(userService).getAllTasksByUserId("user-id");
    }

    @Test
    void testGetPendingTasksByUserId() {
        when(userService.getPendingTaskById("user-id")).thenReturn(taskList);

        ResponseEntity<CommonResponse<List<TaskResponse>>> response = userController.getPendingTasksByUserId("user-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tasks retrieved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskList, response.getBody().getData());
        verify(userService).getPendingTaskById("user-id");
    }

    @Test
    void testGetTasksWithFeedback() {
        when(userService.getTasksWithFeedback("user-id")).thenReturn(taskWithFeedbackList);

        ResponseEntity<CommonResponse<List<TaskWithFeedbackResponse>>> response = userController.getTasksWithFeedback("user-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tasks with feedback retrieved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskWithFeedbackList, response.getBody().getData());
        verify(userService).getTasksWithFeedback("user-id");
    }

    @Test
    void testApproveTask() {
        when(userService.requestApprovalTask("task-id")).thenReturn(taskResponse);

        ResponseEntity<CommonResponse<String>> response = userController.approveTask("task-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task request approved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskResponse.getStatus().toString(), response.getBody().getData());
        verify(userService).requestApprovalTask("task-id");
    }
}