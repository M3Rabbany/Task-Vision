package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.model.dto.response.KpiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;

public interface DashboardService {
    Page<DashboardResponse> getFilteredDashboards(String search, LocalDate start, LocalDate end, String filterBy, Pageable pageable);
    Page<KpiResponse> getUserKpiMetrics(int page, int size, String name);

}
