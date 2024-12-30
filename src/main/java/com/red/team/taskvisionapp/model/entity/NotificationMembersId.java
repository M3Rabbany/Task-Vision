package com.red.team.taskvisionapp.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;


@AllArgsConstructor
@NoArgsConstructor
public class NotificationMembersId implements Serializable {
    private String user;
    private String notification;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationMembersId)) return false;
        NotificationMembersId that = (NotificationMembersId) o;
        return Objects.equals(user, that.user) && Objects.equals(notification, that.notification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, notification);
    }

}
