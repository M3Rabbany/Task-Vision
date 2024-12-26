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
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest projectRequest) {
        ProjectResponse createdProject = projectService.createProject(projectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable String id, @RequestBody ProjectRequest projectRequest) {
        ProjectResponse updatedProject = projectService.updateProject(id, projectRequest);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<CommonResponse<String>> assignUserToProject(@RequestBody ProjectAssignRequest request) {
        projectService.assignUserToProject(request);
        return ResponseEntity
                .ok(CommonResponse
                        .<String>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("User assigned to project successfully!")
                        .build());
    }

    @PutMapping("{id}/status")
    public ResponseEntity<CommonResponse<ProjectResponse>> updateStatusProject(
                @PathVariable String id,
                @RequestBody UpdateProjectStatusRequest request
    ) {
       request.setProjectId(id);
        ProjectResponse projectResponse = projectService.updateStatus(request);

        return ResponseEntity
                .ok(CommonResponse
                        .<ProjectResponse>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Project status updated successfully!")
                        .data(projectResponse)
                        .build());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<List<ProjectResponse>>> getProjectById(
            @PathVariable String userId
    ) {
        List<ProjectResponse> projects = projectService.getAllProjectsByUserId(userId);

        return ResponseEntity
                .ok(CommonResponse
                        .<List<ProjectResponse>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Projects found!")
                        .data(projects)
                        .build());
    }
}
