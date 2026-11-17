package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Task t WHERE t.assignedTo.id = :userId")
    int deleteByAssignedToId(@Param("userId") String userId);

}
