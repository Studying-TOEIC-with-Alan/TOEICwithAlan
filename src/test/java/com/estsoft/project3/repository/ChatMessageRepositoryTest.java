package com.estsoft.project3.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.ChatMessage;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(MockS3ClientConfig.class)
@ActiveProfiles("test")
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1, user2, user3;
    private ChatMessage msg1, msg2, msg3;

    @BeforeEach
    void setUp() {
        // given
        user1 = createUser("user1@example.com", 1L);
        user2 = createUser("user2@example.com", 1L);
        user3 = createUser("user3@example.com", 1L);

        msg1 = new ChatMessage(user1, 1, "Hello");
        msg2 = new ChatMessage(user2, 1, "Hi");
        msg3 = new ChatMessage(user3, 1, "Old");

        setSentAt(msg1, LocalDateTime.now().minusMinutes(10));
        setSentAt(msg2, LocalDateTime.now().minusMinutes(5));
        setSentAt(msg3, LocalDateTime.now().minusDays(2));

        chatMessageRepository.saveAll(List.of(msg1, msg2, msg3));
    }

    @Test
    @DisplayName("시간순 조회")
    void findByRoomIdOrderBySentAtAsc() {
        // when
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(1);

        // then
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getMessage()).isEqualTo("Old");
        assertThat(messages.get(1).getMessage()).isEqualTo("Hello");
        assertThat(messages.get(2).getMessage()).isEqualTo("Hi");
    }

    @Test
    @DisplayName("오래된 메시지 삭제")
    void deleteBySentAtBefore() {
        // given
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);

        // when
        chatMessageRepository.deleteBySentAtBefore(cutoff);

        // then
        List<ChatMessage> remaining = chatMessageRepository.findAll();
        assertThat(remaining).hasSize(2);
        assertThat(remaining).allMatch(msg -> msg.getSentAt().isAfter(cutoff));
    }

    private User createUser(String email, Long grade) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .provider("google")
                    .email(email)
                    .nickname("tester")
                    .role(Role.ROLE_USER)
                    .isActive("Y")
                    .build()
            ));
        user.setGrade(grade);
        return user;
    }

    private void setSentAt(ChatMessage message, LocalDateTime time) {
        try {
            var field = ChatMessage.class.getDeclaredField("sentAt");
            field.setAccessible(true);
            field.set(message, time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
