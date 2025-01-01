package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.request.JwtClaims;
import com.red.team.taskvisionapp.model.entity.User;

public interface JwtService {
    String generateToken(User user);
    boolean verifyJwtToken(String token);
    JwtClaims getJwtClaims(String token);
    String validateAndExtractEmail(String token);
}
