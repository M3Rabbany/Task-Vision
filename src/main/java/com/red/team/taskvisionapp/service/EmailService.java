package com.red.team.taskvisionapp.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);

    void sendWelcomeEmail(String to, String name, String role);
}
