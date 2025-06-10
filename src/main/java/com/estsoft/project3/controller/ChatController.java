package com.estsoft.project3.controller;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.ChatMessageDto;
import com.estsoft.project3.repository.UserRepository;
import com.estsoft.project3.service.ChatService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(
        @RequestParam int roomId,
        @RequestBody String message,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!canEnter(user.getGrade(), roomId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        chatService.sendMessage(user, roomId, message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/receive")
    public DeferredResult<List<ChatMessageDto>> receiveMessage(
        @RequestParam int roomId,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!canEnter(user.getGrade(), roomId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return chatService.receiveMessage(roomId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChatMessageDto>> getAllMessages(
        @RequestParam int roomId,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!canEnter(user.getGrade(), roomId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(chatService.getAllMessages(roomId));
    }

    private boolean canEnter(Long grade, int roomId) {
        return grade >= roomId;
    }

    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = principal.getAttribute("email");
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        return ResponseEntity.ok(Map.of(
            "nickname", user.getNickname(),
            "email", user.getEmail(),
            "grade", user.getGrade()
        ));
    }
}