package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.request.TaskRequest;
import com.red.team.taskvisionapp.model.dto.response.TaskResponse;
import com.red.team.taskvisionapp.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/v1/projects/{projectId}/tasks
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable UUID projectId) {
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // POST /api/v1/projects/{projectId}/tasks
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable UUID projectId, @RequestBody TaskRequest taskRequest) {
        taskRequest.setProjectId(projectId);  // Ensure the projectId is set in the taskRequest
        TaskResponse taskResponse = taskService.createTask(taskRequest);
        return new ResponseEntity<>(taskResponse, HttpStatus.CREATED);
    }

    // PUT /api/v1/projects/{projectId}/tasks/{taskId}
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody TaskRequest taskRequest) {
        taskRequest.setProjectId(projectId);  // Ensure the projectId is set in the taskRequest
        TaskResponse taskResponse = taskService.updateTask(taskId, taskRequest);
        return new ResponseEntity<>(taskResponse, HttpStatus.OK);
    }

    // DELETE /api/v1/projects/{projectId}/tasks/{taskId}
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
