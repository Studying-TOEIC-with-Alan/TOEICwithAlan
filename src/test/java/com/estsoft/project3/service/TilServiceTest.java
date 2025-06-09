package com.estsoft.project3.service;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.Til;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.TilRequest;
import com.estsoft.project3.repository.TilRepository;
import com.estsoft.project3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TilServiceTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TilRepository tilRepository;

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

        tilRepository.deleteAll();
    }

    @Test
    void insertTIL() {
        //given:
        TilRequest request = new TilRequest(user.getUserId(), "Title1", "Summary1");

        //when:
        tilService.insertTIL(request);

        //then:
        List<Til> til = tilRepository.findAll();

        assert til.size() == 1;
        assertEquals(til.get(0).getTitle(), request.getTitle());
        assertEquals(til.get(0).getSummary(), request.getSummary());
    }

    @Test
    void getTILsByUserId() {
        //given:
        Til savedTil1 = new Til(user, "Title1", "Summary1");
        tilRepository.save(savedTil1);
        Til savedTil2 = new Til(user, "Title1", "Summary1");
        tilRepository.save(savedTil2);
        Pageable pageable = PageRequest.of(0, 10);

        //when:
        Page<Til> tilPage = tilService.getTILsByUserId(user.getUserId(), pageable);

        //then:
        assertEquals(2, tilPage.getTotalElements());
    }

    @Test
    void getTIL() {
        //given:
        Til savedTil1 = new Til(user, "Title1", "Summary1");
        tilRepository.save(savedTil1);
        Til savedTil2 = new Til(user, "Title1", "Summary1");
        tilRepository.save(savedTil2);

        //when:
        Til til1 = tilService.getTIL(savedTil1.getTilId());
        Til til2 = tilService.getTIL(savedTil1.getTilId());

        //then:
        assert til1 != null;
        assert til2 != null;
        assertEquals(til1.getTitle(), savedTil1.getTitle());
        assertEquals(til1.getSummary(), savedTil1.getSummary());
        assertEquals(til2.getTitle(), savedTil2.getTitle());
        assertEquals(til2.getSummary(), savedTil2.getSummary());
    }

    @Test
    void updateTIL() {
        //given:
        Til savedTil = new Til(user, "Title", "Summary");
        tilRepository.save(savedTil);

        TilRequest updateTil = new TilRequest(user.getUserId(), "New Title", "New Summary");

        //when:
        tilService.updateTIL(savedTil.getTilId(), updateTil);

        //then:
        Optional<Til> tilInfo = tilRepository.findById(savedTil.getTilId());

        assert tilInfo.isPresent();
        assertEquals("New Title", tilInfo.get().getTitle());
        assertEquals("New Summary", tilInfo.get().getSummary());
    }

    @Test
    void deleteTIL() {
        //given:
        Til savedTil = new Til(user, "Title", "Summary");
        tilRepository.save(savedTil);

        //when:
        tilService.deleteTIL(savedTil.getTilId());

        //then:
        Optional<Til> tilInfo = tilRepository.findById(savedTil.getTilId());

        assert tilInfo.isEmpty();
    }
}