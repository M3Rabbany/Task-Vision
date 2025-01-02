package com.red.team.taskvisionapp.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.red.team.taskvisionapp.constant.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportRequest {
    @JsonProperty("project_id")
    private String projectId;
}
