package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.model.dto.response.NotificationResponse;
import com.red.team.taskvisionapp.model.entity.Notification;
import com.red.team.taskvisionapp.model.entity.NotificationMember;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.NotificationMemberRepository;
import com.red.team.taskvisionapp.repository.NotificationRepository;
import com.red.team.taskvisionapp.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMemberRepository notificationMemberRepository;

    @Override
    public List<NotificationResponse> getAllNotificationsForUser(User user) {
        List<Notification> notifications = notificationMemberRepository.findByUser(user)
                .stream()
                .map(NotificationMember::getNotification)
                .toList();
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationResponse> getFilteredNotifications(
            User user,
            String search,
            TypeNotification filterBy,
            Boolean read,
            Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        String keyword = StringUtils.hasText(search) ? search.trim() : null;

        Page<Notification> notifications = notificationRepository.findFilteredNotificationsForUser(
                user.getId(), keyword, filterBy, read, effectivePageable);
        return notifications.map(this::mapToResponse);
    }

    @Override
    public void markNotificationAsRead(String id, User user) {
        NotificationMember membership = notificationMemberRepository.findByNotificationIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        Notification notification = membership.getNotification();
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setContent(notification.getContent());
        response.setType(notification.getType().toString());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
