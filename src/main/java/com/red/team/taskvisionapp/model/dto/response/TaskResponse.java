package com.red.team.taskvisionapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private String id;
    private String projectId;
    private String assignedTo;
    private String taskName;
    private LocalDateTime deadline;
    private String status;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
