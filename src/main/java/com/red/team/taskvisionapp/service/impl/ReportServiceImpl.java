package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.ReportType;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.request.ReportRequest;
import com.red.team.taskvisionapp.model.dto.response.ReportResponse;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.model.entity.Report;
import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.repository.ReportRepository;
import com.red.team.taskvisionapp.service.ExcelExportService;
import com.red.team.taskvisionapp.service.ReportService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ValidationService validationService;
    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final ExcelExportService excelExportService;

    @Override
    public byte[] generateReport(ReportType reportType) throws IOException {


        List<String> headers = Arrays.asList("Project Name", "Task Name", "Status", "Deadline", "KPI", "Feedback");

        // Data isi
        List<List<String>> data = Arrays.asList(
                Arrays.asList("Project A", "Task 1", "Completed", "2024-12-27", "80", "Task completed successfully"),
                Arrays.asList("Project B", "Task 2", "In Progress", "2024-12-28", "70", "Need additional resources")
        );

        // Generate file Excel
        return excelExportService.generateExcelFile(data, headers);
    }

    @Override
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public ReportResponse createReport(ReportRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Report report = new Report();
        report.setType(request.getType());
        report.setProject(project);
        reportRepository.save(report);
        return mapToResponse(report);
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse
                .builder()
                .id(report.getId())
                .projectName(report.getProject().getProjectName())
                .projectId(report.getProject().getId())
                .type(report.getType())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
