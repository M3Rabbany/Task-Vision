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
public class FeedbackResponse {
    private String id;
    private TaskResponse task;
    private String title;
    private String feedback;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
