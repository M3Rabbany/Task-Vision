package com.red.team.taskvisionapp.service.impl;

import com.red.team.taskvisionapp.model.dto.response.UsersResponse;
import com.red.team.taskvisionapp.model.entity.Users;
import com.red.team.taskvisionapp.repository.UsersRepository;
import com.red.team.taskvisionapp.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;

    @Override
    public UsersResponse getUserById(String id) {
        Users users = usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER NOT FOUND"));
        return convertToUsersResponse(users);
    }

    @Override
    public List<UsersResponse> getAllUsersByName(String name) {

        List<Users> users;
        if (name != null && !name.isEmpty()) {
            users = usersRepository.findAllByNameLikeOrderByNameAsc("%" + name + "%");
            if (users.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "USER NOT FOUND");
            }
        } else {
            users = usersRepository.findAll();
        }

        return users.stream().map(this::convertToUsersResponse).collect(Collectors.toList());
    }

    private UsersResponse convertToUsersResponse(Users users) {
        return UsersResponse.builder()
                .id(users.getId())
                .name(users.getName())
                .email(users.getEmail())
                .password(users.getPassword())
                .role(users.getRole())
                .contact(users.getContact())
                .kpi(users.getKpi())
                .createdAt(Timestamp.from(Instant.from(Instant.now())))
                .updatedAt(Timestamp.from(Instant.from(Instant.now())))
                .build();
    }
}
