package com.estsoft.project3.service;

import com.estsoft.project3.dto.QuizQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ToeicParserServiceTest {

    @Autowired
    private ToeicParserService toeicParserService;

    @Test
    void parse() {
        //given:
        String sampleInput = """
            ### Passage:
            This is sample passage.

            ### Question:
            This is a sample question text

            ### Choices:
            A) choice 1
            B) choice 2
            C) choice 3
            D) choice 4

            ### Answer:
            C) choice 3
        """;

        //when:
        QuizQuestion question = toeicParserService.parse(sampleInput);

        //then:
        assertNotNull(question);
        assertEquals("This is sample passage.", question.getPassage());
        assertEquals("This is a sample question text", question.getQuestion());

        Map<String, String> choices = question.getAnswerChoices();
        assertEquals(4, choices.size());
        assertEquals("choice 1", choices.get("A"));
        assertEquals("choice 2", choices.get("B"));
        assertEquals("choice 3", choices.get("C"));
        assertEquals("choice 4", choices.get("D"));

        assertEquals("C", question.getCorrectAnswer());
    }
}