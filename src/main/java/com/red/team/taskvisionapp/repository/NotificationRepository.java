package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query("SELECT n FROM Notification n WHERE " +
            "(:search IS NULL OR n.content LIKE %:search%) AND " +
            "(:filterBy IS NULL OR n.type = :filterBy)") // Direct comparison for enum
    Page<Notification> findFilteredNotifications(String search, TypeNotification filterBy, Pageable pageable);
}
