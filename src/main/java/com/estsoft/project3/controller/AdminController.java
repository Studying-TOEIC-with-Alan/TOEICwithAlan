package com.estsoft.project3.controller;


import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import com.estsoft.project3.service.AdminService;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin")
    public String adminPage(@RequestParam(required = false) String nickname,
        @PageableDefault(size = 10) Pageable pageable,
        @AuthenticationPrincipal OAuth2User principal,
        Model model) {

        System.out.println("현재 사용자 이메일: " + principal.getAttribute("email"));
        System.out.println("현재 권한: " + principal.getAuthorities());

        Page<User> userPage = (nickname == null || nickname.isBlank())
            ? adminService.findAll(pageable)
            : adminService.findByNicknameContaining(nickname, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("nickname", nickname);

        return "admin";
    }

    @PostMapping("/admin/update")
    public String updateUserInfo(@RequestParam Long userId, @RequestParam Long grade,
        @RequestParam Long score) {
        adminService.updateUserGradeAndScore(userId, grade, score);
        return "redirect:/admin";
    }

}
