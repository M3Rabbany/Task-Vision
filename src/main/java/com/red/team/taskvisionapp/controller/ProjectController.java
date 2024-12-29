package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.request.ProjectAssignRequest;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.request.UpdateProjectStatusRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.ProjectResponse;
import com.red.team.taskvisionapp.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.PROJECTS)
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<ProjectResponse>>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(CommonResponse.<List<ProjectResponse>>builder()
                .message("Projects retrieved successfully!")
                .data(projects)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping
    public ResponseEntity<CommonResponse<ProjectResponse>> createProject(@RequestBody ProjectRequest projectRequest) {
        ProjectResponse createdProject = projectService.createProject(projectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.<ProjectResponse>builder()
                .message("Project created successfully!")
                .data(createdProject)
                .statusCode(HttpStatus.CREATED.value())
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ProjectResponse>> updateProject(@PathVariable String id, @RequestBody ProjectRequest projectRequest) {
        ProjectResponse updatedProject = projectService.updateProject(id, projectRequest);
        return ResponseEntity.ok(CommonResponse.<ProjectResponse>builder()
                .message("Project updated successfully!")
                .data(updatedProject)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(CommonResponse.<Void>builder()
                .message("Project deleted successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/assign")
    public ResponseEntity<CommonResponse<String>> assignProjectToUser(
            @RequestBody ProjectAssignRequest request
    ) {
        projectService.assignUserToProject(request);
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("User assigned to project successfully!")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PutMapping("{projectId}/status")
    public ResponseEntity<CommonResponse<ProjectResponse>> updateStatusProject(
            @PathVariable String projectId,
            @RequestBody UpdateProjectStatusRequest request
    ) {
        request.setProjectId(projectId);
        ProjectResponse projectResponse = projectService.updateStatus(request);
        return ResponseEntity.ok(CommonResponse.<ProjectResponse>builder()
                .message("Project status updated successfully!")
                .data(projectResponse)
                .statusCode(HttpStatus.OK.value())
                .build());
    }


}
