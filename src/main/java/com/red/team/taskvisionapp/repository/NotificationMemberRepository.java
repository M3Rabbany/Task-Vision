package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.NotificationMember;
import com.red.team.taskvisionapp.model.entity.NotificationMembersId;
import com.red.team.taskvisionapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationMemberRepository extends JpaRepository<NotificationMember, NotificationMembersId> {
    List<NotificationMember> findByUser(User user);
}

