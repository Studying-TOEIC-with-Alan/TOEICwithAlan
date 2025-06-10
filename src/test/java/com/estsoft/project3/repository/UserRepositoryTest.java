package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private User createTestUser(String provider, String email, String nickname, String isActive, LocalDate terminationDate) {
        User user = new User();
        user.setProvider(provider);
        user.setEmail(email);
        user.setNickname(nickname);
        user.setRole(Role.ROLE_USER);
        user.setIsActive(isActive);
        user.setTerminationDate(terminationDate);
        return userRepository.save(user);
    }

    @Test
    void findByEmailAndIsActive() {
        //given:
        User savedUser1 = createTestUser("google", "email@test.com", "Nickname1", "Y", LocalDate.now());
        User savedUser2 = createTestUser("google", "email@test.com", "Nickname2", "N", LocalDate.now());

        //when:
        Optional<User> userInfo = userRepository.findByEmailAndIsActive("email@test.com","Y");

        //then:
        assert userInfo.isPresent();
        assertEquals(savedUser1.getProvider(), userInfo.get().getProvider());
        assertEquals(savedUser1.getEmail(), userInfo.get().getEmail());
        assertEquals(savedUser1.getNickname(), userInfo.get().getNickname());
        assertEquals(savedUser1.getRole(), userInfo.get().getRole());
        assertEquals(savedUser1.getIsActive(), userInfo.get().getIsActive());
    }

    @Test
    void findByEmailAndIsActiveAndTerminationDateAfter() {
        //given:
        User savedUser1 = createTestUser("google", "email@test.com", "Nickname1", "N", LocalDate.now().minusDays(8));
        User savedUser2 = createTestUser("google", "email@test.com", "Nickname2", "N", LocalDate.now().minusDays(3));
        LocalDate terminateAfter = LocalDate.now().minusDays(7);

        //when:
        Optional<User> userInfo = userRepository.findByEmailAndIsActiveAndTerminationDateAfter("email@test.com","N", terminateAfter);

        //then:
        assert userInfo.isPresent();
        assertEquals(savedUser2.getProvider(), userInfo.get().getProvider());
        assertEquals(savedUser2.getEmail(), userInfo.get().getEmail());
        assertEquals(savedUser2.getNickname(), userInfo.get().getNickname());
        assertEquals(savedUser2.getRole(), userInfo.get().getRole());
        assertEquals(savedUser2.getIsActive(), userInfo.get().getIsActive());
    }

    @Test
    void findByUserIdAndIsActive() {
        //given:
        User savedUser = createTestUser("google", "email@test.com", "Nickname", "Y", LocalDate.now());

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
        User savedUser = createTestUser("google", "email@test.com", "Nickname", "Y", LocalDate.now());

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

    @Test
    void findByNicknameContaining() {
        //given:
        User savedUser1 = createTestUser("google", "email1@test.com", "Nickname1", "Y", LocalDate.now());
        User savedUser2 = createTestUser("kakao", "email2@test.com", "OtherName1", "Y", LocalDate.now());
        User savedUser3 = createTestUser("facebook", "email2@test.com", "OtherName2", "Y", LocalDate.now());
        Pageable pageable = PageRequest.of(0, 10);

        //when:
        Page<User> userPage = userRepository.findByNicknameContaining("Nickname", pageable);

        //then:
        assert userPage != null;
        assertEquals(1,userPage.getTotalPages());
        assertEquals(1,userPage.getTotalElements());

        List<User> userList = userPage.getContent();
        assertEquals(savedUser1.getProvider(), userList.get(0).getProvider());
        assertEquals(savedUser1.getEmail(), userList.get(0).getEmail());
        assertEquals(savedUser1.getNickname(), userList.get(0).getNickname());
    }

    @Test
    void findAll() {
        //given:
        User savedUser1 = createTestUser("google", "email1@test.com", "Nickname1", "Y", LocalDate.now());
        User savedUser2 = createTestUser("kakao", "email2@test.com", "Nickname2", "Y", LocalDate.now());
        User savedUser3 = createTestUser("facebook", "email3@test.com", "Nickname3", "Y", LocalDate.now());
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 2);

        //when:
        Page<User> userPage1 = userRepository.findAll(pageable1);
        Page<User> userPage2 = userRepository.findAll(pageable2);

        //then:
        assertEquals(1,userPage1.getTotalPages());
        assertEquals(3,userPage1.getTotalElements());
        assertEquals(3,userPage1.getContent().size());

        assertEquals(2,userPage2.getTotalPages());
        assertEquals(3,userPage2.getTotalElements());
        assertEquals(1,userPage2.getContent().size());
    }
}