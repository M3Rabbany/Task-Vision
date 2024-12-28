package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;

public interface DashboardRepository extends JpaRepository<Project, String> {

    Page<Project> findByDescriptionContainingAndCreatedAtBetween(String description, LocalDate startDate, LocalDate endDate, Pageable pageable);


    Page<Project> findByDescriptionContainingAndDeadlineBetween(String description, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
