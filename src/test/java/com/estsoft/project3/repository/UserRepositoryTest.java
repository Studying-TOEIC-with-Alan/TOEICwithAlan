package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private User createTestUser() {
        User user = new User();
        user.setProvider("google");
        user.setEmail("email@test.com");
        user.setNickname("Nickname");
        user.setRole(Role.ROLE_USER);
        user.setIsActive("Y");
        return userRepository.save(user);
    }

    @Test
    void findByEmail() {
        //given:
        User savedUser = createTestUser();

        //when:
        Optional<User> userInfo = userRepository.findByEmail("email@test.com");

        //then:
        assert userInfo.isPresent();
        assertEquals(savedUser.getProvider(), userInfo.get().getProvider());
        assertEquals(savedUser.getEmail(), userInfo.get().getEmail());
        assertEquals(savedUser.getNickname(), userInfo.get().getNickname());
        assertEquals(savedUser.getRole(), userInfo.get().getRole());
        assertEquals(savedUser.getIsActive(), userInfo.get().getIsActive());
    }

    @Test
    void findByUserIdAndIsActive() {
        //given:
        User savedUser = createTestUser();

        //when:
        Optional<User> userInfo = userRepository.findByUserIdAndIsActive(savedUser.getUserId(), "Y");

        //then:
        assert userInfo.isPresent();
        assertEquals(savedUser.getProvider(), userInfo.get().getProvider());
        assertEquals(savedUser.getEmail(), userInfo.get().getEmail());
        assertEquals(savedUser.getNickname(), userInfo.get().getNickname());
        assertEquals(savedUser.getRole(), userInfo.get().getRole());
        assertEquals(savedUser.getIsActive(), userInfo.get().getIsActive());
    }

    @Test
    void findByNickname() {
        //given:
        User savedUser = createTestUser();

        //when:
        User userInfo = userRepository.findByNickname("Nickname");

        //then:
        assert userInfo != null;
        assertEquals(savedUser.getProvider(), userInfo.getProvider());
        assertEquals(savedUser.getEmail(), userInfo.getEmail());
        assertEquals(savedUser.getNickname(), userInfo.getNickname());
        assertEquals(savedUser.getRole(), userInfo.getRole());
        assertEquals(savedUser.getIsActive(), userInfo.getIsActive());
    }
}