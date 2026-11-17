package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.constant.UserRole;
import com.red.team.taskvisionapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findFirstById(String id);
    Page<User> findAllByRoleAndNameContainingIgnoreCase(UserRole role, String name, Pageable pageable);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByContact(String contact);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM User u WHERE u.id = :id")
    int deleteUserById(@Param("id") String id);

}
