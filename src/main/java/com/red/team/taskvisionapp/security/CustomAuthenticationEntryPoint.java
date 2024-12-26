package com.red.team.taskvisionapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // Set HTTP Status

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        // Custom response body
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", 403);
        responseBody.put("error", "Access Denied: " + authException.getMessage());
        responseBody.put("message", "You do not have permission to access this resource");
        responseBody.put("path", request.getRequestURI());

        // Convert to JSON and write to response
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}

