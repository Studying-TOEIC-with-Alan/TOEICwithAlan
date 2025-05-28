package com.estsoft.project3.login.controller;

import com.estsoft.project3.user.domain.User;
import com.estsoft.project3.user.dto.SessionUser;
import com.estsoft.project3.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/set-nickname")
    public String setNickname() {
        return "/login/nickname";
    }

    @PostMapping("/set-nickname")
    public String saveNickname(@RequestParam String nickname,
        @AuthenticationPrincipal OAuth2User principal, HttpSession session) {

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        user.setNickname(nickname);
        userRepository.save(user);

        session.setAttribute("user", new SessionUser(user));
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "/login/login";
    }

}
