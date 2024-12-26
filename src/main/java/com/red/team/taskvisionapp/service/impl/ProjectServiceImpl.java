package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.repository.ProjectRepository;
import com.red.team.taskvisionapp.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
}
