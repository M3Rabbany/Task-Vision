package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    List<Feedback> findByTaskId(String taskId);

    List<Feedback> findByCreatedById(String userId);

    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM Feedback f
            WHERE f.task.id IN (
                SELECT t.id FROM Task t WHERE t.assignedTo.id = :userId
            )
            """)
    int deleteByTaskAssignedToId(@Param("userId") String userId);
}
