package com.red.team.taskvisionapp.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskFeedbackRequest {
    @NotBlank(message = "Project Id is required!")
    private String projectId;
    @NotBlank(message = "Task Id is required!")
    @JsonProperty("task_id")
    private String taskId;
    @NotBlank(message = "Feedback is required!")
    private String title;
    private String feedback;
}
