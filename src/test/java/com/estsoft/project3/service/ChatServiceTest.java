package com.estsoft.project3.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.ChatMessage;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.ChatMessageDto;
import com.estsoft.project3.repository.ChatMessageRepository;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(MockS3ClientConfig.class)
@ActiveProfiles("test")
class ChatServiceTest {

    @Autowired
    ChatService chatService;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void sendAndReceive() throws Exception {
        // given
        User user = User.builder()
            .provider("test-provider")
            .email("tester@example.com")
            .nickname("tester")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();

        user = userRepository.save(user);

        int roomId = 1;
        String message = "Hello";

        DeferredResult<List<ChatMessageDto>> deferredResult = chatService.receiveMessage(roomId);

        // when
        chatService.sendMessage(user, roomId, message);

        // then
        await().atMost(2, TimeUnit.SECONDS).until(() -> deferredResult.getResult() != null);

        @SuppressWarnings("unchecked")
        List<ChatMessageDto> result = (List<ChatMessageDto>) deferredResult.getResult();

        assertNotNull(result);
        assertEquals(1, result.size());

        ChatMessageDto dto = result.get(0);
        assertEquals("tester", dto.getNickname());
        assertEquals(message, dto.getMessage());
        assertNotNull(dto.getSentAt());
    }

    @Test
    void getAll() {
        // given
        User user = User.builder()
            .provider("test-provider")
            .email("tester@example.com")
            .nickname("tester")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();

        user = userRepository.save(user);

        int roomId = 2;
        chatService.sendMessage(user, roomId, "First");
        chatService.sendMessage(user, roomId, "Second");

        // when
        List<ChatMessageDto> messages = chatService.getAllMessages(roomId);

        // then
        assertEquals(2, messages.size());
        assertEquals("First", messages.get(0).getMessage());
        assertEquals("Second", messages.get(1).getMessage());
    }

    @Test
    void deleteOld() {
        // given
        User user = User.builder()
            .provider("test-provider")
            .email("tester@example.com")
            .nickname("tester")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();

        user = userRepository.save(user);

        int roomId = 0;

        ChatMessage oldMessage = new ChatMessage(user, roomId, "Old message",
            LocalDateTime.now().minusMonths(2));
        chatMessageRepository.save(oldMessage);

        chatService.sendMessage(user, roomId, "New message");

        // when
        chatService.deleteOldMessages();

        // then
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
        assertEquals(1, messages.size());
        assertEquals("New message", messages.get(0).getMessage());
    }
}