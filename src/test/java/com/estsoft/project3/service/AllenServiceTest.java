package com.estsoft.project3.service;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Allen;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.AllenRequest;
import com.estsoft.project3.repository.AllenRepository;
import com.estsoft.project3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
class AllenServiceTest {

    @Autowired
    private AllenRepository allenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AllenService allenService;

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

        allenRepository.deleteAll();
    }

    private void createAllen(User user,String category, String inputText, String summary, LocalDateTime createdDate) {
        Allen allen = new Allen();
        allen.setUser(user);
        allen.setCategory(category);
        allen.setInputText(inputText);
        allen.setSummary(summary);
        allen.setCreatedDate(createdDate);
        allenRepository.save(allen);
    }

    @Test
    void insertAllen() {
        //given:
        AllenRequest allenRequest = new AllenRequest(user.getUserId(), "category", "input text", "summary");

        //when:
        allenService.insertAllen(allenRequest);

        //then:
        List<Allen> allen = allenRepository.findAll();

        assert allen.size() == 1;
        assertEquals(allenRequest.getCategory(), allen.get(0).getCategory());
        assertEquals(allenRequest.getInputText(), allen.get(0).getInputText());
        assertEquals(allenRequest.getSummary(), allen.get(0).getSummary());
    }

    @Test
    void getLastAllenByUserAndCatAndInput() {
        //given:
        String category = "category";
        String inputText = "input text";

        createAllen(user, category, inputText, "summary1", LocalDateTime.now().minusMinutes(1));
        createAllen(user, category, inputText, "summary2", LocalDateTime.now());

        //when:
        Allen allenResult = allenService.GetLastAllenByUserAndCatAndInput(user.getUserId(), category, inputText);

        //then:
        assertEquals(category, allenResult.getCategory());
        assertEquals(inputText, allenResult.getInputText());
        assertEquals("summary2", allenResult.getSummary());
    }
}