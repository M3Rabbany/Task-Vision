package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.request.TaskApproveRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskAssignRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskFeedbackRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // GET /api/v1/projects/{projectId}/tasks
    @GetMapping
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getTasksByProject(@PathVariable String projectId) {
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    // POST /api/v1/projects/{projectId}/tasks
    @PostMapping
    public ResponseEntity<CommonResponse<TaskResponse>> createTask(
            @PathVariable String projectId,
            @RequestBody TaskRequest taskRequest) {
        taskRequest.setProjectId(projectId);  // Ensure projectId is set in the taskRequest
        TaskResponse createdTask = taskService.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.<TaskResponse>builder()
                .message("Task created successfully!")
                .data(createdTask)
                .statusCode(HttpStatus.CREATED.value())
                .build());
    }

    // PUT /api/v1/projects/{projectId}/tasks/{taskId}
    @PutMapping("/{taskId}")
    public ResponseEntity<CommonResponse<TaskResponse>> updateTask(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestBody TaskRequest taskRequest) {
        taskRequest.setProjectId(projectId);  // Ensure projectId is set in the taskRequest
        TaskResponse updatedTask = taskService.updateTask(taskId, taskRequest);
        return ResponseEntity.ok(CommonResponse.<TaskResponse>builder()
                .message("Task updated successfully!")
                .data(updatedTask)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    // DELETE /api/v1/projects/{projectId}/tasks/{taskId}
    @DeleteMapping("/{taskId}")
    public ResponseEntity<CommonResponse<Void>> deleteTask(
            @PathVariable String projectId,
            @PathVariable String taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .message("Task deleted successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<CommonResponse<String>> assignTask(TaskAssignRequest request) {
        taskService.assignTaskToMember(request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task assigned successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/{taskId}/approve")
    public ResponseEntity<CommonResponse<String>> approveTask(TaskApproveRequest request) {
        taskService.approveTask(request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task approved successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/{taskId}/feedback")
    public ResponseEntity<CommonResponse<String>> feedbackTask(TaskFeedbackRequest request) {
        taskService.feedbackTask(request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task rejected successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{userId}/tasks")
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getAllTasksByUserId(@PathVariable String userId) {
        List<TaskResponse> tasks = taskService.getAllTasksByUserId(userId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{userId}/tasks/pending")
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getPendingTasksByUserId(@PathVariable String userId) {
        List<TaskResponse> tasks = taskService.getPendingTaskById(userId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{userId}/tasks/{taskId}/feedback")
    public ResponseEntity<CommonResponse<String>> getTaskFeedback(@PathVariable String userId, @PathVariable String taskId) {
        TaskResponse feedback = taskService.getTaskById(taskId, userId);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task feedback retrieved successfully!")
                .data(feedback.getFeedback())
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PutMapping("{taskId}/approval")
    public ResponseEntity<CommonResponse<Void>> approveTask(@PathVariable String taskId) {
        taskService.requestApprovalTask(TaskApproveRequest.builder().taskId(taskId).build());
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .message("Task request approved successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{userId}/tasks/status")
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getTasksByStatus(@PathVariable String userId) {
        List<TaskResponse> tasks = taskService.getTasksByStatus(userId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }
}
