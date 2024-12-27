package com.red.team.taskvisionapp.service;


import com.red.team.taskvisionapp.constant.ReportType;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.request.ReportRequest;
import com.red.team.taskvisionapp.model.dto.response.ReportResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReportService {

    byte[] generateReport(ReportType reportType);

    List<ReportResponse> getAllReports();

    ReportResponse createReport(ReportRequest request);
}
