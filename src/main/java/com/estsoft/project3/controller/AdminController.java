package com.estsoft.project3.controller;


import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.contact.ContactRequestDto;
import com.estsoft.project3.contact.ContactResponseDto;
import com.estsoft.project3.contact.ContactService;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.UserRepository;
import com.estsoft.project3.service.AdminService;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final ContactService contactService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, ContactService contactService, UserRepository userRepository) {
        this.adminService = adminService;
        this.contactService = contactService;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin")
    public String adminPage(@RequestParam(required = false) String nickname,
        @PageableDefault(size = 10) Pageable pageable,
        Model model,
        HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");

        Page<User> userPage = (nickname == null || nickname.isBlank())
            ? adminService.findAll(pageable)
            : adminService.findByNicknameContaining(nickname, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("userId", sessionUser.getUserId());
        model.addAttribute("role", String.valueOf(sessionUser.getRole()));
        model.addAttribute("nickname", sessionUser.getNickname());
        model.addAttribute("isActive", sessionUser.getIsActive());

        return "admin";
    }

    @PostMapping("/admin/update")
    public String updateUserInfo(@RequestParam Long userId, @RequestParam Long grade,
        @RequestParam Long score) {
        adminService.updateUserGradeAndScore(userId, grade, score);
        return "redirect:/admin";
    }

    @GetMapping("/admin/contact")
    public String showAllContacts(
        @RequestParam(name = "sort", defaultValue = "newest") String sort,
        @PageableDefault(size = 10) Pageable pageable,
        Model model,
        @AuthenticationPrincipal OAuth2User principal, HttpSession httpSession) {

        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");

        System.out.println("현재 권한: " + principal.getAuthorities());
        boolean newestFirst = sort.equalsIgnoreCase("newest");

        Page<ContactResponseDto> dtoPage = adminService.getPagedContactsForAdmin(newestFirst,
            pageable);

        model.addAttribute("contactPage", dtoPage); // Page 객체
        model.addAttribute("sort", sort);
        model.addAttribute("userId", sessionUser.getUserId());
        model.addAttribute("role", String.valueOf(sessionUser.getRole()));
        model.addAttribute("nickname", sessionUser.getNickname());

        return "adminContact";
    }

    @PostMapping("/admin/contact/update")
    public String updateContactStatus(@RequestParam Long contactId,
        @RequestParam String status) {
        contactService.updateContactStatus(contactId, status);
        return "redirect:/admin/contact";
    }

    @GetMapping("/admin/contact/view/{contactId}")
    public String viewContactDetail(@PathVariable Long contactId, Model model) {
        Contact contact = contactService.getContactById(contactId);
        ContactResponseDto dto = new ContactResponseDto(contact);
        model.addAttribute("contact", dto);
        return "contact-detail";
    }

}
