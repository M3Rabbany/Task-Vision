package com.red.team.taskvisionapp.model.dto.request;

import com.red.team.taskvisionapp.constant.Roles;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersRequest {
    @NotBlank(message = "Name is required!")
    private String name;
    @NotBlank(message = "email is required!")
    private String email;
    @NotBlank(message = "password is required!")
    private String password;
    @NotBlank(message = "role is required!")
    private Roles role;
    @NotBlank(message = "contact is required!")
    private String contact;
}
