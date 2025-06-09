package com.estsoft.project3.repository;

import com.estsoft.project3.domain.Allen;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AllenRepositoryTest {

    @Autowired
    private AllenRepository allenRepository;

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
    void findAllByUserUserIdAndCategoryAndInputText() {
        //given:
        String category = "Category";
        String inputText = "InputText";

        createAllen(user,category,inputText,"summary1",LocalDateTime.now());
        createAllen(user,category,inputText,"summary2",LocalDateTime.now());

        //when:
        List<Allen> allenList = allenRepository.findAllByUserUserIdAndCategoryAndInputText(user.getUserId(), category, inputText);

        //then:
        assert allenList != null;
        assert allenList.size() == 2;
    }

    @Test
    void findFirstByUserUserIdAndCategoryAndInputTextStartingWithOrderByCreatedDateDesc() {
        //given:
        String category = "Category";

        createAllen(user,category,"InputText1","summary1",LocalDateTime.now().minusMinutes(1));
        createAllen(user,category,"InputText2","summary2",LocalDateTime.now());

        //when:
        Allen allen = allenRepository.findFirstByUserUserIdAndCategoryAndInputTextStartingWithOrderByCreatedDateDesc(user.getUserId(), category,"InputText");

        //then:
        assert allen != null;
        assertEquals("InputText2",allen.getInputText());
        assertEquals("summary2",allen.getSummary());
    }
}