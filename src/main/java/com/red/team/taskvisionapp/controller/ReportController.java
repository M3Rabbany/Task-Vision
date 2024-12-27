package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.constant.ReportType;
import com.red.team.taskvisionapp.model.dto.request.ReportRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.ReportResponse;
import com.red.team.taskvisionapp.repository.ReportRepository;
import com.red.team.taskvisionapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam ReportType type
    ) throws IOException {
        byte[] reports = reportService.generateReport(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(reports);
    }
}
