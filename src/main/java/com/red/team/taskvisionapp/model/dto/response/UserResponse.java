package com.red.team.taskvisionapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String contact;
    private Float kpi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

