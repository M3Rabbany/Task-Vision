package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.ReportType;
import com.red.team.taskvisionapp.constant.TaskStatus;
import com.red.team.taskvisionapp.model.dto.request.ProjectRequest;
import com.red.team.taskvisionapp.model.dto.request.ReportRequest;
import com.red.team.taskvisionapp.model.dto.response.*;
import com.red.team.taskvisionapp.model.entity.Project;
import com.red.team.taskvisionapp.model.entity.Report;
import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.repository.ReportRepository;
import com.red.team.taskvisionapp.service.ExcelExportService;
import com.red.team.taskvisionapp.service.ReportService;
import com.red.team.taskvisionapp.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ValidationService validationService;
    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final ExcelExportService excelExportService;


    @Override
    public byte[] generateReport(ReportRequest request) throws IOException {
        ReportResponse response = createReport(request);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet projectDetails = workbook.createSheet("Project Details");
            createProjectDetailsSheet(workbook, response, projectDetails);

            Sheet taskDetails = workbook.createSheet("Task Details");
            createTaskDetailsSheet(workbook, response.getTasks(), taskDetails);

            Sheet members = workbook.createSheet("Members");
            createMembersSheet(workbook, members, response.getMembers());

            Sheet activities = workbook.createSheet("Activities");
            createActivitiesSheet(workbook, activities, response.getActivities());

            // Convert workbook to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate report.");
        }

    }

    private void createProjectDetailsSheet(Workbook workbook, ReportResponse project, Sheet sheet) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Project Name");
        headerRow.createCell(1).setCellValue(project.getProjectName());

        Row statusRow = sheet.createRow(1);
        statusRow.createCell(0).setCellValue("Status");
        statusRow.createCell(1).setCellValue(project.getStatus());

        Row progressRow = sheet.createRow(2);
        progressRow.createCell(0).setCellValue("Progress");
        progressRow.createCell(1).setCellValue(project.getProgress());

        Row descriptionRow = sheet.createRow(3);
        descriptionRow.createCell(0).setCellValue("Description");
        descriptionRow.createCell(1).setCellValue(project.getDescription());

        Row createdRow = sheet.createRow(4);
        createdRow.createCell(0).setCellValue("Created At");
        createdRow.createCell(1).setCellValue(formatDateTime(project.getCreatedAt()));

        Row updatedRow = sheet.createRow(5);
        updatedRow.createCell(0).setCellValue("Updated At");
        updatedRow.createCell(1).setCellValue(formatDateTime(project.getCreatedAt()));

        // Apply styles
        for (int i = 0; i <= 4; i++) {
            sheet.getRow(i).getCell(0).setCellStyle(headerStyle);
        }

        // Adjust column widths
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTaskDetailsSheet(Workbook workbook, List<ReportTask> list, Sheet sheet) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Task Name", "Deadline", "Status", "Assigned To", "Feedbacks"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ReportTask task : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(task.getTaskName());
            row.createCell(1).setCellValue(formatDateTime(task.getDeadline()));
            row.createCell(2).setCellValue(task.getStatus());
            row.createCell(3).setCellValue(task.getAssignedTo());
            row.createCell(4).setCellValue(task.getFeedbacks().stream()
                    .map(ReportFeedback::toString)
                    .collect(Collectors.joining(", ")));
        }

        // Adjust column widths
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMembersSheet(Workbook workbook, Sheet sheet, List<ReportMember> members) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Email", "Role", "KPI"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (ReportMember member : members) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(member.getName());
            row.createCell(1).setCellValue(member.getEmail());
            row.createCell(2).setCellValue(member.getRole());
            row.createCell(3).setCellValue(member.getKpi());
        }

        // Adjust column widths
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createActivitiesSheet(Workbook workbook, Sheet sheet, List<ReportActivity> activities) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Entity", "Action", "Details", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (ReportActivity activity : activities) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(activity.getEntity());
            row.createCell(1).setCellValue(activity.getAction());
            row.createCell(2).setCellValue(activity.getDetails());
            row.createCell(3).setCellValue(formatDateTime(activity.getCreatedAt()));
        }

        // Adjust column widths
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    @Override
    public List<ReportResponse> getAllReports() {
        return null;
    }

    @Override
    public ReportResponse createReport(ReportRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        ReportResponse response = new ReportResponse();
        response.setProjectName(project.getProjectName());
        response.setStatus(project.getStatus().name());
        response.setDescription(project.getDescription());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setMembers(project.getUsers().stream().map(user -> {
            ReportMember member = new ReportMember();
            member.setName(user.getName());
            member.setEmail(user.getEmail());
            member.setRole(user.getRole().name());
            member.setKpi(user.getKpi());
            return member;
        }).collect(Collectors.toList()));
        response.setTasks(project.getTasks().stream().map(task -> {
            ReportTask taskResponse = new ReportTask();
            taskResponse.setTaskName(task.getTaskName());
            taskResponse.setDeadline(task.getDeadline());
            taskResponse.setStatus(task.getStatus().name());
            taskResponse.setAssignedTo(task.getAssignedTo().getName());
            taskResponse.setFeedbacks(task.getFeedbacks().stream().map(feedback -> {
                ReportFeedback reportFeedback = new ReportFeedback();
                reportFeedback.setTitle(feedback.getTitle());
                reportFeedback.setFeedback(feedback.getFeedback());
                reportFeedback.setCreatedAt(feedback.getCreatedAt());
                return reportFeedback;
            }).collect(Collectors.toList()));
            return taskResponse;
        }).collect(Collectors.toList()));
        response.setActivities(project.getActivities().stream().map(activity -> {
            ReportActivity activityResponse = new ReportActivity();
            activityResponse.setEntity(activity.getEntity());
            activityResponse.setAction(activity.getAction());
            activityResponse.setDetails(activity.getDetails());
            activityResponse.setCreatedAt(activity.getCreatedAt());
            return activityResponse;
        }).collect(Collectors.toList()));
        long approvedTasks = project.getTasks().stream().filter(task -> task.getStatus() == TaskStatus.APPROVED).count();
        long totalTasks = (long) project.getTasks().size();
        Long progress = approvedTasks * 100 / totalTasks;
        response.setProgress(progress);
        return response;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
