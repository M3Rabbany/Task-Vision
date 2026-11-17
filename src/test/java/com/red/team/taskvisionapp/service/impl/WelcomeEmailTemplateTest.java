package com.red.team.taskvisionapp.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WelcomeEmailTemplateTest {

    @Test
    void htmlIncludesBrandingRoleAndLoginLink() {
        String html = WelcomeEmailTemplate.html(
                "Budi Santoso",
                "budi@example.com",
                "PROJECT_MANAGER",
                "https://taskvision.example.com/login"
        );

        assertTrue(html.contains("Task Vision"));
        assertTrue(html.contains("Halo, Budi Santoso!"));
        assertTrue(html.contains("budi@example.com"));
        assertTrue(html.contains("Project Manager"));
        assertTrue(html.contains("Kelola project, bagi task"));
        assertTrue(html.contains("https://taskvision.example.com/login"));
        assertTrue(html.contains("Masuk ke Task Vision"));
    }

    @Test
    void htmlEscapesUserProvidedValues() {
        String html = WelcomeEmailTemplate.html(
                "Budi & <script>alert('x')</script>",
                "budi+test@example.com",
                "MEMBER",
                "https://taskvision.example.com/login"
        );

        assertTrue(html.contains("Budi &amp; &lt;script&gt;"));
        assertFalse(html.contains("<script>alert"));
    }

    @Test
    void plainTextIncludesRoleSpecificDescription() {
        String text = WelcomeEmailTemplate.plainText(
                "Siti Aminah",
                "siti@example.com",
                "ADMIN",
                "https://taskvision.example.com/login"
        );

        assertTrue(text.contains("Halo, Siti Aminah!"));
        assertTrue(text.contains("Peran: Administrator"));
        assertTrue(text.contains("Akses penuh untuk mengelola user, project"));
        assertTrue(text.contains("https://taskvision.example.com/login"));
    }
}
