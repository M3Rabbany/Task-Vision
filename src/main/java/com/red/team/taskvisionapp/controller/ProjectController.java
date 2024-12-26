package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.PROJECTS)
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
}
