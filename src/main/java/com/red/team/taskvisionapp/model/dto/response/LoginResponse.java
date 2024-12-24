package com.red.team.taskvisionapp.model.dto.response;

import com.red.team.taskvisionapp.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String email;
    private String token;
    private String role;
}
