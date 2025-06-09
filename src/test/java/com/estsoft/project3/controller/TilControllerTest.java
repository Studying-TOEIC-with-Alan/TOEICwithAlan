package com.estsoft.project3.controller;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.Til;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.TilRepository;
import com.estsoft.project3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TilControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        User user = new User();
        user.setProvider("google");
        user.setEmail("email@test.com");
        user.setNickname("Nickname");
        user.setRole(Role.ROLE_USER);
        user.setIsActive("Y");
        return userRepository.save(user);
    }

    private Til createTestTil() {
        User user = createUser();

        Til til = new Til();
        til.setUser(user);
        til.setTitle("Title");
        til.setSummary("Summary");
        return tilRepository.save(til);
    }

    @BeforeEach
    void clearDB() {
        tilRepository.deleteAll();
    }

    @Test
    void insertTIL() throws Exception {
        //given:
        User user = createUser();

        String jsonContent = String.format("""
                {
                    "userId": %d,
                    "title": "Title",
                    "summary": "Summary"
                }
                """,user.getUserId());

        //when:
        ResultActions resultActions = mockMvc.perform(post("/api/til",user.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        //then:
        resultActions.andExpect(status().isOk());

        Til insertedTil = tilRepository.findAll().get(0);
        assertEquals("Title", insertedTil.getTitle());
        assertEquals("Summary", insertedTil.getSummary());
    }

    @Test
    void updateTIL() throws Exception {
        //given:
        Til savedTil = createTestTil();

        String jsonContent = """
                {
                    "title": "NewTitle",
                    "summary": "NewSummary"
                }
                """;

        //when:
        ResultActions resultActions = mockMvc.perform(put("/api/til/{id}",savedTil.getTilId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        //then:
        resultActions.andExpect(status().isOk());

        Til tilInfo = tilRepository.findById(savedTil.getTilId()).orElseThrow();
        assertEquals("NewTitle", tilInfo.getTitle());
        assertEquals("NewSummary", tilInfo.getSummary());
    }

    @Test
    void deleteTIL() throws Exception {
        //given:
        Til savedTil = createTestTil();

        //when:
        ResultActions resultActions = mockMvc.perform(delete("/api/til/{id}",savedTil.getTilId()));

        //then:
        resultActions.andExpect(status().isOk());

        Optional<Til> tilInfo = tilRepository.findById(savedTil.getTilId());
        assert tilInfo.isEmpty();
    }
}