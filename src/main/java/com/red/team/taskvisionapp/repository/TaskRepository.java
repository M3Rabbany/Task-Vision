package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByProjectId(String projectId);
    List<Task> findByAssignedToId(String assignedTo);
    List<Task> findByAssignedToIdAndStatus(String assignedTo_id, TaskStatus status);
    Optional<Task> findByIdAndAssignedToId(String taskId, String userId);


}
