package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.TestRequest;
import com.red.team.taskvisionapp.model.dto.response.TestResponse;

import java.util.List;

public interface TestService {
    TestResponse createTest(TestRequest request);
    List<TestResponse> getAllTests();
    TestResponse getTestById(String id);
    TestResponse updateTest(TestRequest request);
    void deleteTest(String id);
}

