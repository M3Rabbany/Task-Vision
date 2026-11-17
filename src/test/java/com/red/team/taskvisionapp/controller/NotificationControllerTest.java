package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.NotificationResponse;
import com.red.team.taskvisionapp.model.entity.Notification;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    private User user;
    private NotificationResponse notificationResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("user-id");
        user.setName("Test User");
        user.setEmail("test@email.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList()));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Notification notification = new Notification();
        notification.setId("notification-id");
        notification.setContent("Test Notification");
        notification.setType(TypeNotification.INFO);
        notification.setRead(false);

        notificationResponse = new NotificationResponse();
        notificationResponse.setId(notification.getId());
        notificationResponse.setContent(notification.getContent());
        notificationResponse.setType(notification.getType().toString());
        notificationResponse.setRead(notification.isRead());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetAllNotificationsForUser () {
        when(userRepository.findById("user-id")).thenReturn(Optional.of(user));
        when(notificationService.getAllNotificationsForUser (user)).thenReturn(Collections.singletonList(notificationResponse));

        ResponseEntity<CommonResponse<List<NotificationResponse>>> response = notificationController.getAllNotificationsForUser ("user-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notifications found!", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(notificationResponse, response.getBody().getData().get(0));
        verify(userRepository, times(1)).findById("user-id");
        verify(notificationService, times(1)).getAllNotificationsForUser (user);
    }

    @Test
    public void testGetFilteredNotifications() {
        Pageable pageable = Pageable.ofSize(10);
        Page<NotificationResponse> notificationPage = new PageImpl<>(Collections.singletonList(notificationResponse));
        when(notificationService.getFilteredNotifications(user, "Test", TypeNotification.INFO, null, pageable))
                .thenReturn(notificationPage);

        ResponseEntity<CommonResponse<Page<NotificationResponse>>> response = notificationController
                .getFilteredNotifications("Test", TypeNotification.INFO, null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).getData().getSize());
        assertEquals(notificationResponse, response.getBody().getData().getContent().get(0));
        verify(notificationService, times(1))
                .getFilteredNotifications(user, "Test", TypeNotification.INFO, null, pageable);
    }
}