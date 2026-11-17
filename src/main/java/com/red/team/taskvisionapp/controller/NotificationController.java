package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.NotificationResponse;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CommonResponse<List<NotificationResponse>>> getAllNotificationsForUser(@PathVariable String userId) {
        User currentUser = getCurrentUser();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!currentUser.getId().equals(user.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's notifications");
        }

        List<NotificationResponse> notifications = notificationService.getAllNotificationsForUser(user);
        return ResponseEntity.ok(CommonResponse.<List<NotificationResponse>>builder()
                .message("Notifications found!")
                .data(notifications)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/filter")
    public ResponseEntity<CommonResponse<Page<NotificationResponse>>> getFilteredNotifications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TypeNotification filterBy,
            @RequestParam(required = false) Boolean read,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NotificationResponse> notifications = notificationService.getFilteredNotifications(
                getCurrentUser(), search, filterBy, read, pageable);
        return ResponseEntity.ok(CommonResponse.<Page<NotificationResponse>>builder()
                .message("Notifications found!")
                .statusCode(HttpStatus.OK.value())
                .data(notifications)
                .build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<CommonResponse<String>> markNotificationAsRead(@PathVariable String id) {
        notificationService.markNotificationAsRead(id, getCurrentUser());
        return ResponseEntity.ok(CommonResponse.<String>builder()
                .message("Notification marked as read")
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
