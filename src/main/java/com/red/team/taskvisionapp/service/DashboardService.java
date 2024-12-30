package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.model.dto.response.KpiResponse;
import com.red.team.taskvisionapp.model.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {
    Page<DashboardResponse> getFilteredDashboards(String search, LocalDate start, LocalDate end, String filterBy, Pageable pageable);
    List<KpiResponse> getUserKpiMetrics();;

}
