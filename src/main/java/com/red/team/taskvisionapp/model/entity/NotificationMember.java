package com.red.team.taskvisionapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_members")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(NotificationMembersId.class)
public class NotificationMember {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;
}
