package com.estsoft.project3.controller;

import com.estsoft.project3.domain.Allen;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.QuizQuestion;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.repository.AllenRepository;
import com.estsoft.project3.repository.UserRepository;
import com.estsoft.project3.service.AllenApiService;
import com.estsoft.project3.service.AllenService;
import com.estsoft.project3.service.ToeicParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AllenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AllenRepository allenRepository;

    @Autowired
    private UserRepository userRepository;

    @InjectMocks
    private AllenController allenController;

    @Mock
    private AllenService allenService;

    @Mock
    private AllenApiService allenApiService;

    @Mock
    private ToeicParserService toeicParserService;

    private User user;

    @BeforeEach
    void setUp() {
        allenRepository.deleteAll();
    }

    private User createUser() {
        User user = new User();
        user.setProvider("google");
        user.setEmail("email@test.com");
        user.setNickname("Nickname");
        user.setRole(Role.ROLE_USER);
        user.setIsActive("Y");
        return userRepository.save(user);
    }

    @Test
    void insertAllen() throws Exception {
        //given:
        User savedUser = createUser();

        String jsonContent = String.format("""
                {
                    "userId": %d,
                    "category": "category",
                    "inputText": "inputText",
                    "summary": "summary"
                }
                """,savedUser.getUserId());

        //when:
        ResultActions resultActions = mockMvc.perform(post("/api/allen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        //then:
        resultActions.andExpect(status().isOk());

        Allen allen = allenRepository.findAll().get(0);
        assertEquals("category", allen.getCategory());
        assertEquals("inputText", allen.getInputText());
        assertEquals("summary", allen.getSummary());
    }

    @Test
    void resetState() throws Exception {
        //given:
        String url = "/api/resetAllen";

        //when:
        ResultActions resultActions = mockMvc.perform(delete(url));

        //then:
        resultActions.andExpect(status().isOk());
    }

    @Test
    void askAllenNonQuiz() throws Exception {
        //given:
        User savedUser = createUser();
        SessionUser sessionUser = new SessionUser(savedUser);

        String category = "문법";
        String inputText = "shopping";

        //when:
        ResultActions resultActions = mockMvc.perform(post("/api/askAllen")
                .sessionAttr("user", sessionUser)
                .param("category",category)
                .param("inputText",inputText));

        //then:
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    void askAllenQuiz() throws Exception {
        //given:
        User savedUser = createUser();
        SessionUser sessionUser = new SessionUser(savedUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", sessionUser);

        String category = "읽기 퀴즈";
        String inputText = "Part 5";
        String mockedRawResponse = "TOEIC Reading question for Part 5";

        QuizQuestion mockQuiz = new QuizQuestion();
        mockQuiz.setPassage("Passage");
        mockQuiz.setQuestion("Question?");
        mockQuiz.setAnswerChoices(Map.of(
                "A", "Choice A",
                "B", "Choice B",
                "C", "Choice C",
                "D", "Choice D"
        ));
        mockQuiz.setCorrectAnswer("B");

        when(allenApiService.getAnswer(anyString())).thenReturn(mockedRawResponse);
        when(toeicParserService.parse(mockedRawResponse)).thenReturn(mockQuiz);
        when(allenService.GetLastAllenByUserAndCatAndInput(savedUser.getUserId(), category, inputText)).thenReturn(null);

        //when:
        ResponseEntity<?> response = allenController.askAllen(category, inputText, session);

        //then:
        assertInstanceOf(QuizQuestion.class, response.getBody());

        QuizQuestion quizQuestion = (QuizQuestion) response.getBody();
        assertEquals("Passage", quizQuestion.getPassage());
        assertEquals("Question?", quizQuestion.getQuestion());
        assertEquals("Choice A", quizQuestion.getAnswerChoices().get("A"));
        assertEquals("Choice B", quizQuestion.getAnswerChoices().get("B"));
        assertEquals("Choice C", quizQuestion.getAnswerChoices().get("C"));
        assertEquals("Choice D", quizQuestion.getAnswerChoices().get("D"));
        assertEquals("B", quizQuestion.getCorrectAnswer());
    }
}