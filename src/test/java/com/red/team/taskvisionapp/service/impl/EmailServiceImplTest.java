package com.red.team.taskvisionapp.service.impl;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceImplTest {

    @Test
    void sendsWelcomeEmailAsMultipartMessage() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));

        EmailServiceImpl emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "sender", "no-reply@example.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://taskvision.example.com/");

        emailService.sendWelcomeEmail("budi@example.com", "Budi", "ADMIN");

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage message = messageCaptor.getValue();
        message.saveChanges();

        assertEquals("Selamat datang di Task Vision!", message.getSubject());
        assertTrue(message.getContentType().startsWith("multipart/"), message.getContentType());
    }
}
