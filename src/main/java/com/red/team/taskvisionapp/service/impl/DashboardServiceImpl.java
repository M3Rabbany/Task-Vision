package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.repository.DashboardRepository;
import com.red.team.taskvisionapp.service.DashboardService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
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
}
