package com.estsoft.project3.controller;

import com.estsoft.project3.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class HomeViewController {
    @GetMapping("/")
    public String showForm(Model model, HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        if (sessionUser != null) {
            model.addAttribute("userId", sessionUser.getUserId());
            model.addAttribute("role", String.valueOf(sessionUser.getRole()));
            model.addAttribute("nickname", sessionUser.getNickname());
        }

        model.addAttribute("categories", List.of("문장 체크","문법", "어휘 목록", "어휘 설명", "일기 퀴즈"));
        model.addAttribute("partList", List.of("Part 5","Part 6", "Part 7"));

        return "index";
    }

}
