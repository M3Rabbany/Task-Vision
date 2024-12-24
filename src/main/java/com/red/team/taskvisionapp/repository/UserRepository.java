package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, String> {

    Optional<UserAccount> findFirstById(String id);
    Optional<UserAccount> findFirstByEmail(String email);
}
