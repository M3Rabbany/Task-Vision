package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ProjectStatus;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.ProjectResponse;
import com.red.team.taskvisionapp.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectControllerTest {
    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectService projectService;

    private ProjectResponse projectResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        projectResponse = ProjectResponse.builder()
                .id("project-id")
                .projectName("Test Project")
                .description("Test Description")
                .status(ProjectStatus.IN_PROGRESS)
                .deadline(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllProjects() {
        when(projectService.getAllProjects()).thenReturn(Collections.singletonList(projectResponse));

        ResponseEntity<CommonResponse<List<ProjectResponse>>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Projects retrieved successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(projectResponse, response.getBody().getData().get(0));
        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    void createProject() {
        ProjectRequest projectRequest = ProjectRequest.builder()
                .projectName("New Project")
                .description("New Description")
                .deadline(LocalDateTime.now().plusDays(10))
                .build();

        when(projectService.createProject(projectRequest)).thenReturn(projectResponse);

        ResponseEntity<CommonResponse<ProjectResponse>> response = projectController.createProject(projectRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Project created successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(projectResponse, response.getBody().getData());
        verify(projectService, times(1)).createProject(projectRequest);
    }

    @Test
    void updateProject() {
        String projectId = "project-id";
        ProjectRequest projectRequest = ProjectRequest.builder()
                .projectName("Updated Project")
                .description("Updated Description")
                .deadline(LocalDateTime.now().plusDays(5))
                .build();

        when(projectService.updateProject(projectId, projectRequest)).thenReturn(projectResponse);

        ResponseEntity<CommonResponse<ProjectResponse>> response = projectController.updateProject(projectId, projectRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Project updated successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(projectResponse, response.getBody().getData());
        verify(projectService, times(1)).updateProject(projectId, projectRequest);
    }

    @Test
    void deleteProject() {
        String projectId = "project-id";

        doNothing().when(projectService).deleteProject(projectId);

        ResponseEntity<CommonResponse<Void>> response = projectController.deleteProject(projectId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Project deleted successfully!", Objects.requireNonNull(response.getBody()).getMessage());
        verify(projectService, times(1)).deleteProject(projectId);
    }
}