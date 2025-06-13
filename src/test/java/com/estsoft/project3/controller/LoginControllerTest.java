package com.estsoft.project3.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.estsoft.project3.config.AdminControllerTestConfig;
import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({MockS3ClientConfig.class, AdminControllerTestConfig.class})
class LoginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @InjectMocks
    private LoginController loginController;

    private OAuth2User oauthUser;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .provider("google")
            .email("test@example.com")
            .nickname("old")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();

        userRepository.save(user);

        oauthUser = new DefaultOAuth2User(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            Collections.singletonMap("email", "test@example.com"),
            "email"
        );
    }

    @Test
    void login() throws Exception {
        mvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("/login"));
    }

    @Test
    void getNick() throws Exception {
        mvc.perform(get("/set-nickname")
                .with(oauth2Login().oauth2User(oauthUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("/nickname"));
    }

    @Test
    void postNick() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/set-nickname")
                .param("nickname", "newNick")
                .with(oauth2Login().oauth2User(oauthUser))
                .with(csrf())
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        SessionUser sUser = (SessionUser) session.getAttribute("user");
        assertThat(sUser).isNotNull();
        assertThat(sUser.getNickname()).isEqualTo("newNick");

        User updated = userRepository.findByEmailAndIsActive("test@example.com", "Y").orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("newNick");
    }

    @Test
    void postNickNoAuth() throws Exception {
        mvc.perform(post("/set-nickname")
                .param("nickname", "nick"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void logout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new SessionUser(user));

        mvc.perform(get("/logout").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        assertThat(session.isInvalid()).isTrue();
    }
}