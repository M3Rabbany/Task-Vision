package com.red.team.taskvisionapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private String content;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private List<NotificationMemberResponse> notificationMembers;

}

