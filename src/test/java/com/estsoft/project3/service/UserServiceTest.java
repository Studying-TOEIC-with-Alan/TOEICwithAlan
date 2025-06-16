package com.estsoft.project3.service;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.UserRequest;
import com.estsoft.project3.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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
    void getUserById() {
        //given:
        User savedUser = createTestUser();

        //when:
        User userInfo = userService.getUserById(savedUser.getUserId());

        //then:
        assert userInfo != null;
        assertEquals(savedUser.getProvider(), userInfo.getProvider());
        assertEquals(savedUser.getEmail(), userInfo.getEmail());
        assertEquals(savedUser.getNickname(), userInfo.getNickname());
        assertEquals(savedUser.getRole(), userInfo.getRole());
        assertEquals(savedUser.getIsActive(), userInfo.getIsActive());
    }

    @Test
    void updateUserById() {
        //given:
        User user = createTestUser();

        UserRequest request = new UserRequest();
        request.setNickname("NewNickname");
        request.setGrade(2L);
        request.setScore(100L);
        request.setIsActive("N");

        //when:
        userService.updateUserById(user.getUserId(), request);

        //then:
        Optional<User> userInfo = userRepository.findById(user.getUserId());
        assert userInfo.isPresent();
        assertEquals(request.getNickname(), userInfo.get().getNickname());
        assertEquals(request.getGrade(), userInfo.get().getGrade());
        assertEquals(request.getScore(), userInfo.get().getScore());
        assertEquals(request.getIsActive(), userInfo.get().getIsActive());
    }
}