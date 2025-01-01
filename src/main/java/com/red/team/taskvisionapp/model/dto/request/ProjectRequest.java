package com.red.team.taskvisionapp.model.dto.request;

import com.red.team.taskvisionapp.constant.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectRequest {
    private String UserId;
    private String projectName;
    private String description;
    private LocalDateTime deadline;
}

