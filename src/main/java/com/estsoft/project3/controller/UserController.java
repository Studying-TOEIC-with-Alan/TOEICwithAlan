package com.estsoft.project3.controller;

import com.estsoft.project3.dto.UserRequest;
import com.estsoft.project3.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/api/user/{id}")
    public ResponseEntity<Void> updateUser (@PathVariable("id") long id, @RequestBody UserRequest request) {
        userService.updateUserById(id, request);
        return ResponseEntity.ok().build();
    }
}
