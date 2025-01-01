package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.model.dto.request.TaskApproveRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskAssignRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskFeedbackRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.FeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.service.TaskService;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @InjectMocks
    private TaskController taskController;

    @Mock
    private TaskService taskService;

    private String projectId;
    private String taskId;
    private TaskRequest taskRequest;
    private TaskResponse taskResponse;
    private List<TaskResponse> taskList;
    private TaskAssignRequest taskAssignRequest;
    private TaskApproveRequest taskApproveRequest;
    private TaskFeedbackRequest taskFeedbackRequest;
    private FeedbackResponse feedbackResponse;

    @BeforeEach
    public void setUp() {
        projectId = "project-id";
        taskId = "task-id";

        taskRequest = TaskRequest.builder()
                .taskName("Test Task")
                .projectId("project-id")
                .assignedTo("user-id")
                .deadline(LocalDateTime.of(2024, 1, 20, 0, 0))
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .projectId(projectId)
                .taskName("Test Task")
                .assignedTo("user-id")
                .status(TaskStatus.ON_GOING)
                .deadline(LocalDateTime.of(2024, 1, 20, 0, 0))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskList = List.of(taskResponse);

        taskAssignRequest = TaskAssignRequest.builder()
                .projectId("project-id")
                .userId("user-id")
                .taskId("task-id")
                .build();

        taskApproveRequest = TaskApproveRequest.builder()
                .projectId("project-id")
                .taskId("task-id")
                .build();

        taskFeedbackRequest = TaskFeedbackRequest.builder()
                .projectId("project-id")
                .taskId("task-id")
                .title("Feedback Title")
                .feedback("Needs improvement")
                .build();

        feedbackResponse = FeedbackResponse.builder()
                .id("feedback-id")
                .task(TaskResponse.builder()
                        .id("task-id")
                        .taskName("Test Task")
                        .projectId("project-id")
                        .status(TaskStatus.REJECTED)
                        .assignedTo("user-id")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .deadline(LocalDateTime.of(2024, 1, 20, 0, 0))
                        .build())
                .title("Feedback Title")
                .feedback("Needs improvement")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetTasksByProject() {
        when(taskService.getTasksByProject(projectId)).thenReturn(taskList);

        ResponseEntity<CommonResponse<List<TaskResponse>>> response = taskController.getTasksByProject(projectId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tasks retrieved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskList, response.getBody().getData());
        verify(taskService).getTasksByProject(projectId);
    }

    @Test
    void testCreateTask() {
        when(taskService.createTask(taskRequest)).thenReturn(taskResponse);

        ResponseEntity<CommonResponse<TaskResponse>> response = taskController.createTask(projectId, taskRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Task created successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskResponse, response.getBody().getData());
        assertEquals(projectId, taskRequest.getProjectId());
        verify(taskService).createTask(taskRequest);
    }

    @Test
    void testUpdateTask() {
        when(taskService.updateTask(taskId, taskRequest)).thenReturn(taskResponse);

        ResponseEntity<CommonResponse<TaskResponse>> response = taskController.updateTask(projectId, taskId, taskRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task updated successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(taskResponse, response.getBody().getData());
        assertEquals(projectId, taskRequest.getProjectId());
        verify(taskService).updateTask(taskId, taskRequest);
    }

    @Test
    void testDeleteTask() {
        willDoNothing().given(taskService).deleteTask(taskId);

        ResponseEntity<CommonResponse<Void>> response = taskController.deleteTask(projectId, taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task deleted successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        verify(taskService).deleteTask(taskId);
    }

    @Test
    void testFeedbackTask() {
        when(taskService.feedbackTask(taskFeedbackRequest)).thenReturn(feedbackResponse);

        ResponseEntity<CommonResponse<FeedbackResponse>> response = taskController.feedbackTask(taskFeedbackRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task rejected successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        verify(taskService).feedbackTask(taskFeedbackRequest);
    }

    @Test
    void testAssignTaskToMember() {
        willDoNothing().given(taskService).assignTaskToMember(taskAssignRequest);

        ResponseEntity<CommonResponse<String>> response = taskController.assignTaskToMember(taskAssignRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task assigned successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        verify(taskService).assignTaskToMember(taskAssignRequest);
    }

    @Test
    void testApproveTask() {
        willDoNothing().given(taskService).approveTask(taskApproveRequest);

        ResponseEntity<CommonResponse<String>> response = taskController.approveTask(taskApproveRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task approved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        verify(taskService).approveTask(taskApproveRequest);
    }
}