package com.red.team.taskvisionapp.controller;

import com.red.team.taskvisionapp.constant.ApiUrl;
import com.red.team.taskvisionapp.model.dto.response.CommonResponse;
import com.red.team.taskvisionapp.model.dto.response.UsersResponse;
import com.red.team.taskvisionapp.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiUrl.BASE_URL + ApiUrl.USER)
public class UsersController {

    private final UsersService usersService;

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<UsersResponse>> getUserById(@PathVariable String id) {

        UsersResponse usersResponse = usersService.getUserById(id);
        CommonResponse<UsersResponse> response = CommonResponse.<UsersResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users Found")
                .data(usersResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<UsersResponse>>> getAllUsers(String name) {

        List<UsersResponse> usersResponse = usersService.getAllUsersByName(name);
        CommonResponse<List<UsersResponse>> response = CommonResponse.<List<UsersResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users Found")
                .data(usersResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}


