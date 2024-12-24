package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.JwtClaims;
import com.red.team.taskvisionapp.model.entity.UserAccount;

public interface JwtService {
    String generateToken(UserAccount userAccount);
    boolean verifyJwtToken(String token);
    JwtClaims getJwtClaims(String token);
}
