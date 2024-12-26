package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> getAllProjects();
    ProjectResponse createProject(ProjectRequest projectRequest);
    ProjectResponse updateProject(String id, ProjectRequest projectRequest);
    void deleteProject(String id);
}
