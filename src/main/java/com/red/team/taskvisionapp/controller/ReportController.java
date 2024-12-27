package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.request.ReportRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.ReportResponse;
import com.red.team.taskvisionapp.repository.ReportRepository;
import com.red.team.taskvisionapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.REPORT)
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    @PostMapping
    public ResponseEntity<CommonResponse<ReportResponse>>createReport(
            @RequestBody ReportRequest request
    ){
        ReportResponse report = reportService.createReport(request);
        return ResponseEntity.ok(CommonResponse.<ReportResponse>builder()
                .message("Report created successfully!")
                .data(report)
                .statusCode(HttpStatus.CREATED.value())
                .build());
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<ReportResponse>>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(CommonResponse.<List<ReportResponse>>builder()
                .message("Reports retrieved successfully!")
                .data(reports)
                .statusCode(HttpStatus.OK.value())
                .build());
    }
}
