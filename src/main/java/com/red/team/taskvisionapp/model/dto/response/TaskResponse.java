package com.red.team.taskvisionapp.model.dto.response;

import com.red.team.taskvisionapp.constant.TaskStatus;
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
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
