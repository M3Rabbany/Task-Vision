package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.model.dto.response.KpiResponse;
import com.red.team.taskvisionapp.model.entity.Task;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.DashboardRepository;
import com.red.team.taskvisionapp.repository.TaskRepository;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.DashboardService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ValidationService validationService;

    @Override
    public Page<DashboardResponse> getFilteredDashboards(String search, LocalDate start, LocalDate end, String filterBy, Pageable pageable) {

        if (filterBy.equalsIgnoreCase("deadline")) {
            return dashboardRepository.findByDescriptionContainingAndDeadlineBetween(search, start, end, pageable)
                    .map(project -> new DashboardResponse(project.getId(), project.getDescription(), project.getCreatedAt(), project.getDeadline()));
        } else {
            return dashboardRepository.findByDescriptionContainingAndCreatedAtBetween(search, start, end, pageable)
                    .map(project -> new DashboardResponse(project.getId(), project.getDescription(), project.getCreatedAt(), project.getDeadline()));
        }
    }

    @Override
    public List<KpiResponse> getUserKpiMetrics() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {
            List<Task> tasks = taskRepository.findByAssignedToId(user.getId());

            // menghitung metrik tasks
            long totalTasks = tasks.size();
            if (totalTasks == 0) {
                return KpiResponse.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .kpi(0) // kalo gaada task
                        .build();
            }

            long onTimeTasks = tasks.stream()
                    .filter(task -> task.getStatus().equalsIgnoreCase("completed") &&
                            task.getUpdatedAt().isBefore(task.getDeadline()))
                    .count();

            long lateTasks = tasks.stream()
                    .filter(task -> task.getStatus().equalsIgnoreCase("completed") &&
                            task.getUpdatedAt().isAfter(task.getDeadline()))
                    .count();

            // Custom KPI formula
            float kpi = ((onTimeTasks * 1) + (lateTasks * 0.5f)) / totalTasks;

            return KpiResponse.builder()
                    .userId(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .kpi(kpi)
                    .build();
        }).collect(Collectors.toList());
    }
}
