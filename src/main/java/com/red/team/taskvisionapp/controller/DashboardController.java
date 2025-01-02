package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.model.dto.response.KpiResponse;
import com.red.team.taskvisionapp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.DASHBOARD)
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping(ApiUrl.STATUS)
    public ResponseEntity<CommonResponse<Page<DashboardResponse>>> getDashboardStatus(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "filterBy", defaultValue = "createdAt") String filterBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        Page<DashboardResponse> dashboardResponses = dashboardService.getFilteredDashboards(search, start, end, filterBy, pageable);

        CommonResponse<Page<DashboardResponse>> response = CommonResponse.<Page<DashboardResponse>>builder()
                .message("Successfully fetched dashboard status.")
                .statusCode(HttpStatus.OK.value())
                .data(dashboardResponses)
                .build();

        return ResponseEntity.ok(response);
    }

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @GetMapping(ApiUrl.KPI)
    public ResponseEntity<CommonResponse<Page<KpiResponse>>> getUserKpiMetrics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {

        logger.info("Received request to fetch KPI metrics for all users with pagination.");

        Page<KpiResponse> kpiMetricsPage;
        try {
            kpiMetricsPage = dashboardService.getUserKpiMetrics(page, size, name);
            logger.info("Successfully fetched KPI metrics for all users.");
        } catch (Exception e) {
            logger.error("Error fetching KPI metrics for all users.", e);
            throw e;
        }

        return ResponseEntity.ok(CommonResponse.<Page<KpiResponse>>builder()
                .message("Successfully fetched KPI metrics for all users.")
                .statusCode(HttpStatus.OK.value())
                .data(kpiMetricsPage)
                .build());
    }
}


