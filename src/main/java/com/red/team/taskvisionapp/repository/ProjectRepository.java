package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {
}
