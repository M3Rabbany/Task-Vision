package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findFirstById(String id);
    Optional<User> findFirstByEmail(String email);
}
