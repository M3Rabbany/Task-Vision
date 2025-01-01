package com.red.team.taskvisionapp.model.dto.request;

import com.red.team.taskvisionapp.constant.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String name;
    @NotBlank(message = "email is required!")
    private String email;
    @NotBlank(message = "password is required!")
    private String password;
    private UserRole role;
    private String contact;
    private Float kpi;
}
