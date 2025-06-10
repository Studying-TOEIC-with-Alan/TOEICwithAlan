package com.estsoft.project3.controller;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.UserRepository;
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

    @GetMapping("/login")
    public String showLoginPage() {
        return "/login";
    }

    @GetMapping("/set-nickname")
    public String setNickname() {
        return "/nickname";
    }

    @PostMapping("/set-nickname")
    public String saveNickname(@RequestParam String nickname,
        @AuthenticationPrincipal OAuth2User principal, HttpSession session) {

        if (principal == null || principal.getAttribute("email") == null) {
            return "redirect:/login";
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        user.setNickname(nickname);
        userRepository.save(user);

        session.setAttribute("user", new SessionUser(user));
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
