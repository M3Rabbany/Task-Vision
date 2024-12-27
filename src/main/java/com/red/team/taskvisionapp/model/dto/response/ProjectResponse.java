package com.red.team.taskvisionapp.model.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ProjectResponse {
    private String id;
    private String projectName;
    private String description;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @JsonProperty("assigned_to")
    private List<UserResponse> users;
}

