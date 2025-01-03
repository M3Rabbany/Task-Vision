package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.request.TaskApproveRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskAssignRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskFeedbackRequest;
import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.FeedbackResponse;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.PROJECTS + "/{projectId}" + ApiUrl.TASKS)
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/get")
    public ResponseEntity<CommonResponse<List<TaskResponse>>> getTasksByProject(@PathVariable String projectId) {
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(CommonResponse.<List<TaskResponse>>builder()
                .message("Tasks retrieved successfully!")
                .data(tasks)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/create")
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
    public ResponseEntity<CommonResponse<String>> assignTaskToMember(
            @Valid @RequestBody TaskAssignRequest request) {
        log.info("Received TaskAssignRequest: {}", request);

        taskService.assignTaskToMember(request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task assigned successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/{taskId}/approve")
    public ResponseEntity<CommonResponse<String>> approveTask(@RequestBody TaskApproveRequest request) {
        taskService.approveTask(request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Task approved successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/{taskId}" + ApiUrl.FEEDBACK)
    public ResponseEntity<CommonResponse<FeedbackResponse>> feedbackTask(@RequestBody TaskFeedbackRequest request) {
        FeedbackResponse response = taskService.feedbackTask(request);
        return ResponseEntity.ok(CommonResponse.<FeedbackResponse>builder()
                .message("Task rejected successfully!")
                .statusCode(HttpStatus.OK.value())
                .data(response)
                .build());
    }
}
