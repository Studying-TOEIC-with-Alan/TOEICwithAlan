package com.estsoft.project3.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.estsoft.project3.config.AdminControllerTestConfig;
import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.contact.Contact.Status;
import com.estsoft.project3.contact.ContactResponseDto;
import com.estsoft.project3.contact.ContactService;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.service.AdminService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({MockS3ClientConfig.class, AdminControllerTestConfig.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AdminService adminService;
    @Autowired
    private ContactService contactService;

    private SessionUser sessionUser;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUserId(1L);
        testUser.setNickname("testAdmin");
        testUser.setRole(Role.ROLE_ADMIN);
        testUser.setScore(100L);
        testUser.setGrade(2L);
        testUser.setIsActive("Y");

        sessionUser = new SessionUser(testUser);
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void adminPage() throws Exception {
        Page<User> userPage = new PageImpl<>(List.of(new User()));
        when(adminService.findAll(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/admin")
                .sessionAttr("user", sessionUser))
            .andExpect(status().isOk())
            .andExpect(view().name("admin"))
            .andExpect(model().attributeExists("userPage", "users", "userId", "role", "nickname",
                "isActive"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateUserInfo() throws Exception {
        mockMvc.perform(post("/admin/update")
                .param("userId", "1")
                .param("grade", "2")
                .param("score", "100"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("문의 목록 조회")
    void showAllContacts() throws Exception {
        User testUser = User.builder()
            .provider("google")
            .email("test@example.com")
            .nickname("testAdmin")
            .role(Role.ROLE_ADMIN)
            .isActive("Y")
            .build();
        testUser.setUserId(1L);

        Contact testContact = Contact.builder()
            .contactId(1L)
            .user(testUser)
            .title("Test Title")
            .content("Test Content")
            .status(Status.OPEN)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();

        ContactResponseDto dto = new ContactResponseDto(testContact);
        Page<ContactResponseDto> dtoPage = new PageImpl<>(Arrays.asList(dto), PageRequest.of(0, 10),
            1);

        when(adminService.getPagedContactsForAdmin(eq(true), any(Pageable.class))).thenReturn(
            dtoPage);

        OAuth2User mockOAuthUser = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")),
            Map.of("id", "adminUser", "email", "admin@example.com"),
            "id"
        );

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("user", new SessionUser(testUser));

        mockMvc.perform(get("/admin/contact")
                .param("sort", "newest")
                .param("page", "0")
                .param("size", "10")
                .session(mockHttpSession)
                .with(oauth2Login().oauth2User(mockOAuthUser)))
            .andExpect(status().isOk())
            .andExpect(view().name("adminContact"))
            .andExpect(model().attributeExists("contactPage", "sort", "userId", "role", "nickname"))
            .andExpect(model().attribute("contactPage", dtoPage))
            .andExpect(model().attribute("sort", "newest"))
            .andExpect(model().attribute("userId", testUser.getUserId()))
            .andExpect(model().attribute("role", String.valueOf(testUser.getRole())))
            .andExpect(model().attribute("nickname", testUser.getNickname()));
    }

    @Test
    @DisplayName("문의 상태 업데이트")
    void updateContactStatus() throws Exception {
        Long contactId = 1L;
        String status = "CLOSED";

        doNothing().when(contactService).updateContactStatus(contactId, status);

        mockMvc.perform(post("/admin/contact/update")
                .param("contactId", String.valueOf(contactId))
                .param("status", status)
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/contact"));

        verify(contactService).updateContactStatus(contactId, status);
    }

    @Test
    @DisplayName("문의 상세 조회")
    void viewContactDetail() throws Exception {
        Long contactId = 1L;

        User testUser = User.builder()
            .provider("google")
            .email("test@example.com")
            .nickname("testUser")
            .role(Role.ROLE_ADMIN)
            .isActive("Y")
            .build();
        testUser.setUserId(1L);

        Contact contact = Contact.builder()
            .contactId(contactId)
            .user(testUser)
            .title("Test Title")
            .content("Test Content")
            .status(Status.OPEN)
            .build();

        when(contactService.getContactById(contactId)).thenReturn(contact);

        mockMvc.perform(get("/admin/contact/view/{contactId}", contactId)
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(view().name("contact-detail"))
            .andExpect(model().attributeExists("contact"));
    }
}