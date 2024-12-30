package com.red.team.taskvisionapp.service;

import com.red.team.taskvisionapp.model.dto.response.NotificationResponse;
import com.red.team.taskvisionapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getAllNotificationsForUser(User user);
    Page<NotificationResponse> getFilteredNotifications(String search, String filterBy, Pageable pageable);
}
