package com.red.team.taskvisionapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    private String id;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
}
