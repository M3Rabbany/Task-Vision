package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username:}")
    private String sender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (!StringUtils.hasText(sender)) {
            throw new IllegalStateException("MAIL_USERNAME belum dikonfigurasi.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }
}
