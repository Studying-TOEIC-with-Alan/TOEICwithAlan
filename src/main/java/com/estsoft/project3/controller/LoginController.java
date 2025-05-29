package com.estsoft.project3.controller;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    public LoginController(UserRepository userRepository, HttpSession httpSession) {
        this.userRepository = userRepository;
        this.httpSession = httpSession;
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

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        user.setNickname(nickname);
        userRepository.save(user);

        session.setAttribute("user", new SessionUser(user));
        return "redirect:/home";
    }

    //추후 매핑 주소 변경해야함
    @GetMapping("/main")
    public String main(Model model) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        model.addAttribute("nickname", sessionUser.getNickname());
        model.addAttribute("role", sessionUser.getRole());
        model.addAttribute("score", sessionUser.getScore());
        return "main";
    }

}
