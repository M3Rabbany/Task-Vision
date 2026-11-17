package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.constant.TypeNotification;
import com.red.team.taskvisionapp.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query("""
            SELECT n FROM Notification n
            JOIN NotificationMember nm ON nm.notification = n
            WHERE nm.user.id = :userId
              AND (:search IS NULL OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:filterBy IS NULL OR n.type = :filterBy)
              AND (:read IS NULL OR n.isRead = :read)
            """)
    Page<Notification> findFilteredNotificationsForUser(
            @Param("userId") String userId,
            @Param("search") String search,
            @Param("filterBy") TypeNotification filterBy,
            @Param("read") Boolean read,
            Pageable pageable);
}
