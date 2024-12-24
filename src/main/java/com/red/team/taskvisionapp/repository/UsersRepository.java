package com.red.team.taskvisionapp.repository;

import com.red.team.taskvisionapp.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    List<Users> findAllByNameLikeOrderByNameAsc(String name);
}
