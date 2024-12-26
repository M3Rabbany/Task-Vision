package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByAssignedToId(UUID assignedToId);
    List<Task> findByStatus(String status);
}
