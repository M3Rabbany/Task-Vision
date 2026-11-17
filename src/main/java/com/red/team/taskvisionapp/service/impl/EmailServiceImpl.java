package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username:}")
    private String sender;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @PostConstruct
    void logEmailConfiguration() {
        if (!StringUtils.hasText(sender)) {
            log.warn("Email nonaktif: MAIL_USERNAME belum diisi. Email selamat datang dan reset password tidak akan dikirim.");
            return;
        }
        if (!StringUtils.hasText(mailPassword)) {
            log.warn("Email belum siap: MAIL_USERNAME sudah diisi tetapi MAIL_PASSWORD masih kosong.");
            return;
        }
        log.info("Email aktif melalui {}:{}", mailHost, mailPort);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        // Email bersifat opsional. Kegagalan konfigurasi/pengiriman tidak boleh
        // menggagalkan operasi utama seperti membuat user, project, atau task.
        if (isEmailDisabled(to)) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
        } catch (RuntimeException exception) {
            log.warn("Email ke {} tidak dapat dikirim: {}", to, exception.getMessage());
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String name, String role) {
        if (isEmailDisabled(to)) {
            return;
        }

        String recipient = to.trim();
        String displayName = StringUtils.hasText(name) ? name.trim() : "Sobat Task Vision";
        String roleValue = StringUtils.hasText(role) ? role.trim() : "MEMBER";
        String loginUrl = buildLoginUrl();

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(sender, "Task Vision");
            helper.setTo(recipient);
            helper.setSubject("Selamat datang di Task Vision!");
            helper.setText(
                    WelcomeEmailTemplate.plainText(displayName, recipient, roleValue, loginUrl),
                    WelcomeEmailTemplate.html(displayName, recipient, roleValue, loginUrl)
            );

            emailSender.send(message);
        } catch (Exception exception) {
            log.warn("Email selamat datang ke {} tidak dapat dikirim: {}", recipient, exception.getMessage());
        }
    }

    private boolean isEmailDisabled(String to) {
        if (!StringUtils.hasText(sender) || !StringUtils.hasText(to)) {
            log.debug("Email dilewati karena MAIL_USERNAME atau alamat tujuan belum tersedia.");
            return true;
        }
        return false;
    }

    private String buildLoginUrl() {
        String baseUrl = StringUtils.hasText(frontendUrl)
                ? frontendUrl.trim()
                : "http://localhost:8080";
        return baseUrl.replaceAll("/+$", "") + "/login";
    }
}
