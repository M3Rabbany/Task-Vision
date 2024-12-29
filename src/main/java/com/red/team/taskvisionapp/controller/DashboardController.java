package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.model.dto.response.KpiResponse;
import com.red.team.taskvisionapp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.DASHBOARD)
@RequiredArgsConstructor
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/status")
    public CommonResponse<Page<DashboardResponse>> getDashboardStatus(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdDate") String sort,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "filterBy", defaultValue = "createdDate") String filterBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        Page<DashboardResponse> dashboardResponses = dashboardService.getFilteredDashboards(search, start, end, filterBy, pageable);

        return new CommonResponse<>("Success", dashboardResponses, 200);
    }

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpi")
    public ResponseEntity<CommonResponse<List<KpiResponse>>> getUserKpiMetrics() {
        logger.info("Received request to fetch KPI metrics for all users.");

        List<KpiResponse> kpiMetrics;
        try {
            kpiMetrics = dashboardService.getUserKpiMetrics();
            logger.info("Successfully fetched KPI metrics for all users.");
        } catch (Exception e) {
            logger.error("Error fetching KPI metrics for all users.", e);
            throw e;
        }

        return ResponseEntity.ok(new CommonResponse<>("Success", kpiMetrics, 200));
    }

}


