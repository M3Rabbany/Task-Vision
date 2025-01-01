package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findFirstById(String id);
    Optional<User> findFirstByEmail(String email);
    Page<User> findAllByRoleAndNameContainingIgnoreCase(UserRole role, String name, Pageable pageable);


}
