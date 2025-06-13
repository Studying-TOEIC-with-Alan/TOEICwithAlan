package com.estsoft.project3.contact;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    public User createUser(String email) {
        return userRepository.findByEmailAndIsActive(email, "Y").orElseGet(() ->
            userRepository.save(User.builder()
                .email(email)
                .nickname("tester")
                .role(Role.ROLE_USER)
                .isActive("Y")
                .provider("google")
                .build()));
    }

    @Test
    void saveContact() throws Exception {
        createUser("test@example.com");

        ContactRequestDto requestDto = new ContactRequestDto("문의 내용", "문의 제목",
            Collections.emptyList());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        ResultActions resultActions = mockMvc.perform(post("/api/contacts")
            .with(oauth2Login().oauth2User(mockOAuth2User))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto))
        );

        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("문의 제목"))
            .andExpect(jsonPath("$.content").value("문의 내용"));
    }

    @Test
    @DisplayName("문의글 전체 조회")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAllContacts() throws Exception {
        // given
        User testUser = createUser("test@example.com");
        Contact contact = Contact.builder()
            .title("title1")
            .content("content1")
            .user(testUser)
            .build();
        contactRepository.save(contact);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/contacts"));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].title").value("title1"))
            .andExpect(jsonPath("$[0].content").value("content1"));
    }

    @Test
    @DisplayName("ID로 문의글 단건 조회")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getContactById() throws Exception {
        // given
        User testUser = createUser("test@example.com");
        Contact contact = Contact.builder()
            .title("title1")
            .content("content1")
            .user(testUser)
            .build();
        contact = contactRepository.save(contact);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/contacts/{id}", contact.getContactId()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("title1"))
            .andExpect(jsonPath("$.content").value("content1"))
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("문의글 수정")
    void updateContact() throws Exception {
        // given
        User user = createUser("test@example.com");
        Contact contact = Contact.builder()
            .title("old title")
            .content("old content")
            .user(user)
            .build();
        contact = contactRepository.save(contact);

        ContactRequestDto updateDto = new ContactRequestDto("new content", "new title",
            Collections.emptyList());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        // when
        ResultActions resultActions = mockMvc.perform(
            put("/api/contacts/{id}", contact.getContactId())
                .with(oauth2Login().oauth2User(mockOAuth2User))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        );

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("new title"))
            .andExpect(jsonPath("$.content").value("new content"));
    }

    @Test
    @DisplayName("문의글 삭제")
    void deleteContact() throws Exception {
        // given
        User user = createUser("test@example.com");
        Contact contact = Contact.builder()
            .title("to be deleted")
            .content("delete this")
            .user(user)
            .build();
        contact = contactRepository.save(contact);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        // when
        ResultActions resultActions = mockMvc.perform(
            delete("/api/contacts/{id}", contact.getContactId())
                .with(oauth2Login().oauth2User(mockOAuth2User))
        );

        // then
        resultActions.andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("내 문의글 전체 조회")
    void getMyContacts() throws Exception {
        // given
        contactRepository.deleteAll();

        User user = createUser("test@example.com");

        Contact contact = Contact.builder()
            .title("mine")
            .content("my content")
            .user(user)
            .build();
        contactRepository.save(contact);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/contacts/my")
            .with(oauth2Login().oauth2User(mockOAuth2User))
        );

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.title == 'mine')]").exists());
    }

    @Test
    @DisplayName("문의 상태 변경")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateStatus() throws Exception {
        // given
        User admin = createUser("admin@example.com");
        Contact contact = Contact.builder()
            .title("check status")
            .content("status update")
            .user(admin)
            .build();
        contact = contactRepository.save(contact);

        ContactStatusUpdateDto dto = new ContactStatusUpdateDto("IN_PROGRESS");

        // when
        ResultActions resultActions = mockMvc.perform(
            patch("/api/contacts/{id}/status", contact.getContactId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        );

        // then
        resultActions.andExpect(status().isOk());
    }
}