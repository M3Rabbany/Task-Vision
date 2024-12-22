package com.red.team.taskvisionapp.controller;
import com.red.team.taskvisionapp.model.dto.request.TestRequest;
import com.red.team.taskvisionapp.model.dto.response.TestResponse;
import com.red.team.taskvisionapp.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {
    private final TestService testService;
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
}

