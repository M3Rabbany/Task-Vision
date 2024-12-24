package com.red.team.taskvisionapp.model.dto.response;

import com.red.team.taskvisionapp.constant.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersResponse {
    private String id;
    private String name;
    private String email;
    private String password;
    private Roles role;
    private String contact;
    private Float kpi;
    private Date createdAt;
    private Date updatedAt;
}
