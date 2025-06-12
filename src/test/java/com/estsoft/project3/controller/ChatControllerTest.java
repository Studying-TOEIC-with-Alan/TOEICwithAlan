package com.estsoft.project3.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
@Transactional
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        createUser("test@example.com", 2L);
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


    private RequestPostProcessor oauth2Login(String email) {
        return SecurityMockMvcRequestPostProcessors.oauth2Login().oauth2User(
            new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", email),
                "email"
            )
        );
    }

    @Test
    @DisplayName("채팅 메시지 전송 - 정상 처리")
    void testSendMessage_Success() throws Exception {
        Map<String, String> requestBody = Map.of("message", "안녕하세요!");

        mockMvc.perform(post("/api/chat/send")
                .param("roomId", "1")
                .with(oauth2Login("test@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채팅 메시지 전송 - 등급 부족으로 차단")
    void testSendMessage_Forbidden() throws Exception {
        createUser("low@example.com", 1L);

        Map<String, String> requestBody = Map.of("message", "등급 낮음");

        mockMvc.perform(post("/api/chat/send")
                .param("roomId", "5")
                .with(oauth2Login("low@example.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("채팅 메시지 수신 - 비동기 처리 확인")
    void testReceiveMessage_Success() throws Exception {
        mockMvc.perform(get("/api/chat/receive")
                .param("roomId", "1")
                .with(oauth2Login("test@example.com")))
            .andExpect(request().asyncStarted());
    }

    @Test
    @DisplayName("모든 채팅 메시지 조회 - 정상 처리")
    void testGetAllMessages_Success() throws Exception {
        mockMvc.perform(get("/api/chat/all")
                .param("roomId", "1")
                .with(oauth2Login("test@example.com")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("현재 로그인된 사용자 정보 조회 - 정상 처리")
    void testGetCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/chat/users/me")
                .with(oauth2Login("test@example.com")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("tester"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.grade").value(2));
    }
}

