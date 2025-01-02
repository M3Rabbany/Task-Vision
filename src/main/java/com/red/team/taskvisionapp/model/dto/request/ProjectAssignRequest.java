package com.red.team.taskvisionapp.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectAssignRequest {
    @NotBlank(message = "Project Id is required!")
    @JsonProperty("project_id")
    private String projectId;
    @NotNull(message = "User Id cannot be null!")
    @Size(min = 1, message = "User Id cannot be empty!")
    @JsonProperty("user_id")
    private List<String> userId;
}
