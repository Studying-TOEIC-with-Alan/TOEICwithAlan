package com.estsoft.project3.controller;

import com.estsoft.project3.domain.Til;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.service.TilService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TilViewController {
    private final TilService tilService;

    public TilViewController(TilService tilService) {
        this.tilService = tilService;
    }

    @GetMapping("/til")
    public String showTil(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model,
                          HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        Long userId = sessionUser.getUserId();

        Pageable pageable = PageRequest.of(page, size);
        Page<Til> tilList = tilService.getTILsByUserId(userId, pageable);

        //For header
        model.addAttribute("userId", userId);
        model.addAttribute("role", String.valueOf(sessionUser.getRole()));
        model.addAttribute("nickname", sessionUser.getNickname());

        //For TIL
        model.addAttribute("tilList", tilList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tilList.getTotalPages());
        return "til";
    }

    @GetMapping("/til/{tilId}")
    public String showTil(@PathVariable Long tilId, Model model, HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");

        Til til = tilService.getTIL(tilId);

        model.addAttribute("userId", sessionUser.getUserId());
        model.addAttribute("role", sessionUser.getRole());
        model.addAttribute("til", til);

        return "tilDetail";
    }
}
