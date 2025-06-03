package com.estsoft.project3.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class QuizQuestion {
    // Getters and Setters
    private String passage;
    private String question;
    private Map<String, String> answerChoices;
    private String correctAnswer;
}
