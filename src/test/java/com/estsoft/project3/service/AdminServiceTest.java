package com.estsoft.project3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.contact.ContactRepository;
import com.estsoft.project3.contact.ContactResponseDto;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
@AutoConfigureMockMvc
@Transactional
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    private User testUser;
    private Contact testContact1;
    private Contact testContact2;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
            .provider("google")
            .email("test@example.com")
            .nickname("integrationTestUser")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();
        userRepository.save(testUser);

        testContact1 = Contact.builder()
            .createDate(LocalDateTime.of(2023, 1, 1, 10, 0))
            .user(testUser)
            .build();
        testContact2 = Contact.builder()
            .createDate(LocalDateTime.of(2023, 1, 2, 10, 0))
            .user(testUser)
            .build();
        contactRepository.saveAll(List.of(testContact1, testContact2));
    }

    @Test
    @DisplayName("Update User Grade/Score - Success")
    void updateUserGradeAndScore_success() {
        adminService.updateUserGradeAndScore(testUser.getUserId(), 2L, 150L);
        User updatedUser = userRepository.findById(testUser.getUserId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(2L, updatedUser.getGrade());
        assertEquals(150L, updatedUser.getScore());
    }

    @Test
    @DisplayName("Update User Grade/Score - User Not Found")
    void updateUserGradeAndScore_userNotFound_throwsException() {
        Long nonExistentUserId = 999L;
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> adminService.updateUserGradeAndScore(nonExistentUserId, 2L, 150L));
        assertEquals("유저를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("Find All Users Paged")
    void findAll_returnsAllUsersPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = adminService.findAll(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getNickname(), result.getContent().get(0).getNickname());
    }

    @Test
    @DisplayName("Find Users by Nickname Paged")
    void findByNicknameContaining_returnsUsersByNicknamePaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = adminService.findByNicknameContaining("integration", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getNickname(), result.getContent().get(0).getNickname());
    }

    @Test
    @DisplayName("Get Paged Contacts - Newest First")
    void getPagedContactsForAdmin_newestFirst() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ContactResponseDto> result = adminService.getPagedContactsForAdmin(
            true, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(testContact2.getContactId(), result.getContent().get(0).getContactId());
        assertEquals(testContact1.getContactId(), result.getContent().get(1).getContactId());
    }

    @Test
    @DisplayName("Get Paged Contacts - Oldest First")
    void getPagedContactsForAdmin_oldestFirst() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ContactResponseDto> result = adminService.getPagedContactsForAdmin(
            false, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(testContact1.getContactId(), result.getContent().get(0).getContactId());
        assertEquals(testContact2.getContactId(), result.getContent().get(1).getContactId());
    }
}
