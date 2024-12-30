package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.NotificationResponse;
import com.red.team.taskvisionapp.model.entity.Notification;
import com.red.team.taskvisionapp.model.entity.User;
import com.red.team.taskvisionapp.repository.UserRepository;
import com.red.team.taskvisionapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @GetMapping("/user/{userId}")
    public ResponseEntity<CommonResponse<List<NotificationResponse>>> getAllNotificationsForUser (@PathVariable String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User  not found"));

        List<NotificationResponse> notifications = notificationService.getAllNotificationsForUser (user);
        return ResponseEntity.ok(CommonResponse.<List<NotificationResponse>>builder()
                .message("Notifications found!")
                .data(notifications)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<NotificationResponse>> getFilteredNotifications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filterBy,
            Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getFilteredNotifications(search, filterBy, pageable);
        return ResponseEntity.ok(notifications);
    }
}
