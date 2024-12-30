package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.NotificationResponse;
import com.red.team.taskvisionapp.model.entity.Notification;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.NotificationService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private Notification notification;
    private NotificationResponse notificationResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("user-id");
        user.setName("Test User");

        notification = new Notification();
        notification.setId("notification-id");
        notification.setContent("Test Notification");
        notification.setType("INFO");
        notification.setRead(false);

        notificationResponse = new NotificationResponse();
        notificationResponse.setId(notification.getId());
        notificationResponse.setContent(notification.getContent());
        notificationResponse.setType(notification.getType());
        notificationResponse.setRead(notification.isRead());
    }

    @Test
    public void testGetAllNotificationsForUser () {
        when(userRepository.findById("user-id")).thenReturn(Optional.of(user));
        when(notificationService.getAllNotificationsForUser (user)).thenReturn(Arrays.asList(notificationResponse));

        ResponseEntity<CommonResponse<List<NotificationResponse>>> response = notificationController.getAllNotificationsForUser ("user-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notifications found!", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(notificationResponse, response.getBody().getData().get(0));
        verify(userRepository, times(1)).findById("user-id");
        verify(notificationService, times(1)).getAllNotificationsForUser (user);
    }

    @Test
    public void testGetFilteredNotifications() {
        Pageable pageable = Pageable.ofSize(10);
        Page<NotificationResponse> notificationPage = new PageImpl<>(Arrays.asList(notificationResponse));
        when(notificationService.getFilteredNotifications("Test", "INFO", pageable)).thenReturn(notificationPage);

        ResponseEntity<Page<NotificationResponse>> response = notificationController.getFilteredNotifications("Test", "INFO", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(notificationResponse, response.getBody().getContent().get(0));
        verify(notificationService, times(1)).getFilteredNotifications("Test", "INFO", pageable);
    }
}