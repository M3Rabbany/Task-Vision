package com.red.team.taskvisionapp.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskAssignRequest {
    @NotBlank(message = "Project Id is required!")
    @JsonProperty("project_id")
    private String projectId;
    @NotBlank(message = "Task Id is required!")
    @JsonProperty("task_id")
    private String taskId;
    @NotBlank(message = "User Id is required!")
    @JsonProperty("user_id")
    private String userId;
}
