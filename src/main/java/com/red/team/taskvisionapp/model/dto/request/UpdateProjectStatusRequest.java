package com.red.team.taskvisionapp.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.red.team.taskvisionapp.constant.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProjectStatusRequest {
    @JsonIgnore
    private String projectId;
    private ProjectStatus projectStatus;
}
