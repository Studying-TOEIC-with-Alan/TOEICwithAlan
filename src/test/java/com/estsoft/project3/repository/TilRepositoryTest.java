package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.Til;
import com.estsoft.project3.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
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

        //when:
        List<Til> tilList= tilRepository.findAllByUserUserIdOrderByUpdatedDateDesc(user.getUserId());

        //then:
        assert tilList != null;
        assert tilList.size() == 2;
        assert tilList.get(0).getTitle().equals("Title2");
        assert tilList.get(0).getSummary().equals("Summary2");
        assert tilList.get(1).getTitle().equals("Title1");
        assert tilList.get(1).getSummary().equals("Summary1");
    }
}