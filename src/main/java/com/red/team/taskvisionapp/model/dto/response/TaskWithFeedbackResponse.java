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
public class TaskWithFeedbackResponse {
    private String taskId;
    private String feedbackId;
    private String title;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

