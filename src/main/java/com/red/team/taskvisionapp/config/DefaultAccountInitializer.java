package com.red.team.taskvisionapp.config;

import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultAccountInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initializeDefaultAdmin() {
        String defaultEmail = "bismaworkmail@gmail.com";
        String defaultPassword = "admin";

        if (!userRepository.existsByEmail(defaultEmail)) {
            User adminUser = User.builder()
                    .name("Admin")
                    .email(defaultEmail)
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(UserRole.ADMIN)
                    .contact("9999999999")
                    .kpi(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(adminUser);
            System.out.println("Default admin account created with email: " + defaultEmail);
        } else {
            System.out.println("Default admin account already exists.");
        }
    }
}
