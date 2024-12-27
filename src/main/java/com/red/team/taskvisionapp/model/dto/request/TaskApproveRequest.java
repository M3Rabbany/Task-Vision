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
public class TaskApproveRequest {
    @NotBlank(message = "Task Id is required!")
    private String taskId;
    @NotBlank(message = "Project Id is required!")
    private String projectId;
}
