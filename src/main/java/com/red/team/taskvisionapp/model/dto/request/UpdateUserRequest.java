package com.red.team.taskvisionapp.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String contact;
    private Float kpi;
}
