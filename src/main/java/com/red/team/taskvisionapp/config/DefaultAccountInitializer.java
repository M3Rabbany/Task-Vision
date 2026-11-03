package com.red.team.taskvisionapp.config;

import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
public class DefaultAccountInitializer {
    private static final Logger log = LoggerFactory.getLogger(DefaultAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.email:}")
    private String defaultEmail;

    @Value("${app.default-admin.password:}")
    private String defaultPassword;

    public DefaultAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initializeDefaultAdmin() {
        if (!StringUtils.hasText(defaultEmail) || !StringUtils.hasText(defaultPassword)) {
            log.info("Default admin bootstrap dilewati: DEFAULT_ADMIN_EMAIL/DEFAULT_ADMIN_PASSWORD tidak diset.");
            return;
        }

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
            log.info("Default admin account created.");
        } else {
            log.info("Default admin account already exists.");
        }
    }
}
