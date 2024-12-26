package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
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
}
