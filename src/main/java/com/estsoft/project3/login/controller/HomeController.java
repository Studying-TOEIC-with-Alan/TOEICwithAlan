package com.estsoft.project3.login.controller;


import com.estsoft.project3.user.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final HttpSession httpSession;

    public HomeController(HttpSession httpSession) {
        this.httpSession = httpSession;
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
