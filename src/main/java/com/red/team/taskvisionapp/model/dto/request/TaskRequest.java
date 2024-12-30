package com.red.team.taskvisionapp.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskRequest {
    private String projectId;
    private String assignedTo;
    private String taskName;
    private LocalDateTime deadline;
    private String status;
    private String feedback;

}
