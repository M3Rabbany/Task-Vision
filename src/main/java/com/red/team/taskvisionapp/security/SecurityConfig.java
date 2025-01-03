package com.red.team.taskvisionapp.security;

import com.red.team.taskvisionapp.constant.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationFilter authentication;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
                    httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(customAuthenticationEntryPoint);
                })
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("api/v1/notifications/**").permitAll()

                        .requestMatchers("/api/v1/users/get").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/users/create").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/users/{id}").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/projects/get").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/projects/create").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/projects/{id}").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("/api/v1/projects/assign").hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers("api/v1/dashboard/**").hasAuthority(UserRole.ADMIN.name())

                        .requestMatchers("/api/v1/projects/{projectId}/status").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/projects/{projectId}/tasks/get").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/projects/{projectId}/tasks/create").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/projects/{projectId}/tasks/{taskId}").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/projects/{projectId}/tasks/{taskId}/assign").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/projects/{projectId}/tasks/{taskId}/approve").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/projects/{projectId}/users/{userId}").hasAuthority(UserRole.PROJECT_MANAGER.name())
                        .requestMatchers("/api/v1/report/**").hasAuthority(UserRole.PROJECT_MANAGER.name())

                        .requestMatchers("/api/v1/users/{userId}/tasks").hasAuthority(UserRole.MEMBER.name())
                        .requestMatchers("/api/v1/users/{userId}/tasks/pending").hasAuthority(UserRole.MEMBER.name())
                        .requestMatchers("/api/v1/users/{userId}/tasks/feedback").hasAuthority(UserRole.MEMBER.name())
                        .requestMatchers("/api/v1/users/{taskId}/approval").hasAuthority(UserRole.MEMBER.name())


                        .anyRequest().authenticated()
                )
                .addFilterBefore(authentication, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
