package com.estsoft.project3.controller;

import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.dto.UserRequest;
import com.estsoft.project3.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/api/user/{id}")
    public ResponseEntity<Void> updateUser (@PathVariable("id") long id, @RequestBody UserRequest request, HttpSession httpSession) {
        userService.updateUserById(id, request);

        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");

        if (!Objects.equals(sessionUser.getNickname(), request.getNickname())) {
            sessionUser.setNickname(request.getNickname());
            httpSession.setAttribute("user", sessionUser);
        }

        return ResponseEntity.ok().build();
    }
}
