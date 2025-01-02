package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.constant.ReportType;
import com.red.team.taskvisionapp.model.dto.request.ReportRequest;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.ReportResponse;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.service.ProjectService;
import com.red.team.taskvisionapp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.REPORT)
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ProjectRepository projectRepository;
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

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam String projectId
    ) throws IOException {
        ReportRequest request = ReportRequest.builder()
                .projectId(projectId)
                .build();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        byte[] reports = reportService.generateReport(request);
        String filename = "report-" + project.getProjectName() + "-" + Instant.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(reports);
    }
}
