package com.red.team.taskvisionapp.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFeedbackTaskRequest {
    @NotBlank(message = "Task Id is required!")
    private String taskId;
    @NotBlank(message = "User Id is required!")
    private String userId;
}
