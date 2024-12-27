package com.red.team.taskvisionapp.controller;
import com.red.team.taskvisionapp.model.dto.request.TestRequest;
import com.red.team.taskvisionapp.model.dto.response.TestResponse;
import com.red.team.taskvisionapp.service.ExcelExportService;
import com.red.team.taskvisionapp.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {
    private final TestService testService;
    private final ExcelExportService excelExportService;
    @PostMapping
    public ResponseEntity<TestResponse>createTest(
            @RequestBody TestRequest request
    ){
        TestResponse test = testService.createTest(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(test);
    }

    @GetMapping
    public ResponseEntity<List<TestResponse>>getAllTests(){
        List<TestResponse> tests = testService.getAllTests();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tests);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<TestResponse>getTestById(
            @PathVariable("id") String id
    ){
        TestResponse test = testService.getTestById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(test);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<TestResponse>updateTest(
            @RequestBody TestRequest request,
            @PathVariable("id") String id
    ){
        request.setId(id);
        TestResponse test = testService.updateTest(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(test);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<String>deleteTest(
            @PathVariable("id") String id
    ){
        testService.deleteTest(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Successfully delete test with id: " + id);
    }

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {
        // Header kolom
        List<String> headers = Arrays.asList("Project Name", "Task Name", "Status", "Deadline", "KPI", "Feedback");

        // Data isi
        List<List<String>> data = Arrays.asList(
                Arrays.asList("Project A", "Task 1", "Completed", "2024-12-27", "80", "Task completed successfully"),
                Arrays.asList("Project B", "Task 2", "In Progress", "2024-12-28", "70", "Need additional resources")
        );

        // Generate file Excel
        byte[] excelData = excelExportService.generateExcelFile(data, headers);

        // Mengembalikan file sebagai response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}

