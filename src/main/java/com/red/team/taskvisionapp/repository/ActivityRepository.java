package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByProjectId(String projectId);
}
