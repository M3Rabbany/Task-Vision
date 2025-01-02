package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.DashboardResponse;
import com.red.team.taskvisionapp.model.dto.response.KpiResponse;
import com.red.team.taskvisionapp.service.DashboardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardControllerTest {

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private DashboardService dashboardService;

    private DashboardResponse dashboardResponse;

    @BeforeEach
    public void setUp() {
        // Inisialisasi secara manual
        MockitoAnnotations.openMocks(this);

        dashboardResponse = DashboardResponse.builder()
                .id("dashboard-id")
                .description("Test Dashboard")
                .createdAt(LocalDateTime.now())
                .deadline(LocalDateTime.now().plusDays(7))
                .build();

    }
    @Test
    void getDashboardStatus_shouldReturnDashboardResponses() {
        // Arrange
        int page = 0;
        int size = 10;
        String sort = "createdAt";
        String direction = "DESC";
        String search = "test";
        String startDate = "2023-01-01";
        String endDate = "2023-12-31";
        String filterBy = "createdAt";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<DashboardResponse> dashboardPage = new PageImpl<>(List.of(dashboardResponse));

        Mockito.when(dashboardService.getFilteredDashboards(search, LocalDate.parse(startDate), LocalDate.parse(endDate), filterBy, pageable))
                .thenReturn(dashboardPage);

        // Act
        ResponseEntity<CommonResponse<Page<DashboardResponse>>> response = dashboardController.getDashboardStatus(
                page, size, sort, direction, search, startDate, endDate, filterBy);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Successfully fetched dashboard status.", response.getBody().getMessage());
        Assertions.assertEquals(dashboardPage, response.getBody().getData());
    }

    @Test
    void getUserKpiMetrics() {

        int page = 0;
        int size = 10;
        String name = "";

        List<KpiResponse> kpiResponses = List.of(
                KpiResponse.builder()
                        .userId("1")
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .kpi(85.5f)
                        .build(),
                KpiResponse.builder()
                        .userId("2")
                        .name("Jane Doe")
                        .email("jane.doe@example.com")
                        .kpi(90.0f)
                        .build()
        );
        Page<KpiResponse> kpiMetricsPage = new PageImpl<>(kpiResponses);

        Mockito.when(dashboardService.getUserKpiMetrics(page, size, name)).thenReturn(kpiMetricsPage);

        ResponseEntity<CommonResponse<Page<KpiResponse>>> response = dashboardController.getUserKpiMetrics(page, size, name);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "The HTTP status should be 200 OK.");
        Assertions.assertNotNull(response.getBody(), "The response body should not be null.");
        Assertions.assertEquals("Successfully fetched KPI metrics for all users.", response.getBody().getMessage(), "The response message is incorrect.");
        Assertions.assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode(), "The response status code is incorrect.");
        Assertions.assertEquals(kpiMetricsPage, response.getBody().getData(), "The data in the response is incorrect.");

        Mockito.verify(dashboardService, Mockito.times(1)).getUserKpiMetrics(page, size, name);
    }

    @Test
    void getUserKpiMetricsWithNameFilter() {

        int page = 0;
        int size = 10;
        String name = "John";

        List<KpiResponse> kpiResponses = List.of(
                KpiResponse.builder()
                        .userId("1")
                        .name("John Doe")
                        .email("john.doe@example.com")
                        .kpi(85.5f)
                        .build(),
                KpiResponse.builder()
                        .userId("2")
                        .name("John Smith")
                        .email("john.smith@example.com")
                        .kpi(88.0f)
                        .build()
        );
        Page<KpiResponse> kpiMetricsPage = new PageImpl<>(kpiResponses);

        Mockito.when(dashboardService.getUserKpiMetrics(page, size, name)).thenReturn(kpiMetricsPage);

        ResponseEntity<CommonResponse<Page<KpiResponse>>> response = dashboardController.getUserKpiMetrics(page, size, name);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "The HTTP status should be 200 OK.");
        Assertions.assertNotNull(response.getBody(), "The response body should not be null.");
        Assertions.assertEquals("Successfully fetched KPI metrics for all users.", response.getBody().getMessage(), "The response message is incorrect.");
        Assertions.assertEquals(HttpStatus.OK.value(), response.getBody().getStatusCode(), "The response status code is incorrect.");
        Assertions.assertEquals(kpiMetricsPage, response.getBody().getData(), "The data in the response is incorrect.");

        Assertions.assertTrue(response.getBody().getData().getContent().stream()
                .allMatch(kpi -> kpi.getName().contains(name)), "All returned KPIs should match the name filter.");

        Mockito.verify(dashboardService, Mockito.times(1)).getUserKpiMetrics(page, size, name);
    }


}