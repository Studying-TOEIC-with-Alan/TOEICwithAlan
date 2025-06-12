package com.estsoft.project3.controller;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MyPageViewController {
    private final UserService userService;

    public MyPageViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/myPage")
    public String showMyPage(Model model, HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        Long userId = sessionUser.getUserId();

        User user= userService.getUserById(userId);

        //For header
        model.addAttribute("userId", sessionUser.getUserId());
        model.addAttribute("role", String.valueOf(sessionUser.getRole()));
        model.addAttribute("nickname", sessionUser.getNickname());

        //For profile
        model.addAttribute("user", user);
        return "myPage";
    }
}
