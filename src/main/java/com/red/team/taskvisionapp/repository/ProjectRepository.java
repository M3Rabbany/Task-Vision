package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findAllByUsers_Id(String userId); // Get all projects by projectId
}
