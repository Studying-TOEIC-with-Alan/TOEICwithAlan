package com.estsoft.project3.controller;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import com.estsoft.project3.service.ChatRoomAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ChatViewController {

    private final UserRepository userRepository;
    private final ChatRoomAccessService chatRoomAccessService;

    public ChatViewController(UserRepository userRepository,
        ChatRoomAccessService chatRoomAccessService) {
        this.userRepository = userRepository;
        this.chatRoomAccessService = chatRoomAccessService;
    }

    @GetMapping("/chat")
    public String showChatPopup() {
        return "chat-popup";
    }

    @GetMapping("/chat-room")
    public String chatRoomPage(@AuthenticationPrincipal OAuth2User principal,
        @RequestParam("roomId") int roomId,
        Model model) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmailAndIsActive(email,"Y")
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Long grade = user.getGrade();

        if (!chatRoomAccessService.canEnter(grade, roomId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방에 입장할 수 없습니다.");
        }

        model.addAttribute("grade", grade);
        model.addAttribute("roomId", roomId);
        return "chat";
    }

}
