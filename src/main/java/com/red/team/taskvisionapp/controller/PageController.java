package com.red.team.taskvisionapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Routes the browser to the static frontend files that live in
 * {@code src/main/resources/static}. Keeping the pages behind friendly URLs
 * makes the app easier to open and bookmark while still letting Spring Boot
 * serve every asset from the same origin as the REST API.
 */
@Controller
public class PageController {

    @GetMapping({"/", "/login", "/forgot-password"})
    public String loginPage() {
        return "forward:/index.html";
    }

    @GetMapping("/app")
    public String appPage() {
        return "forward:/app.html";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "forward:/reset-password.html";
    }
}
