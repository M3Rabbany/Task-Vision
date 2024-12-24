package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
