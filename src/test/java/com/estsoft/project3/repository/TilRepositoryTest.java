package com.estsoft.project3.repository;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.Til;
import com.estsoft.project3.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
class TilRepositoryTest {

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setProvider("google");
        user.setEmail("email@test.com");
        user.setNickname("Nickname");
        user.setRole(Role.ROLE_USER);
        user.setIsActive("Y");
        userRepository.save(user);
    }

    private void createTestTil(User user, String title, String summary, LocalDateTime updatedDate) {
        Til til = new Til();
        til.setUser(user);
        til.setTitle(title);
        til.setSummary(summary);
        til.setUpdatedDate(updatedDate);
        tilRepository.save(til);
    }

    @Test
    void findAllByUserUserIdOrderByUpdatedDateDesc() {
        //given:
        createTestTil(user, "Title1", "Summary1", LocalDateTime.now().minusMinutes(1));
        createTestTil(user, "Title2", "Summary2", LocalDateTime.now());
        Pageable pageable = PageRequest.of(0, 10);

        //when:
        Page<Til> tilPage = tilRepository.findAllByUserUserIdOrderByUpdatedDateDesc(user.getUserId(), pageable);

        //then:
        assert tilPage != null;
        assertEquals(1,tilPage.getTotalPages());
        assertEquals(2, tilPage.getTotalElements());
        assertEquals(2,tilPage.getContent().size());

        List<Til> tilList = tilPage.getContent();
        assertEquals("Title2", tilList.get(0).getTitle());
        assertEquals("Summary2", tilList.get(0).getSummary());
        assertEquals("Title1", tilList.get(1).getTitle());
        assertEquals("Summary1", tilList.get(1).getSummary());
    }
}