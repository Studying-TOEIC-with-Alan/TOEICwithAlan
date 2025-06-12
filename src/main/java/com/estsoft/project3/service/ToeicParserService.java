package com.estsoft.project3.service;

import com.estsoft.project3.dto.QuizQuestion;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ToeicParserService {
    public QuizQuestion parse(String input) {
        QuizQuestion quizQuestion = new QuizQuestion();
        Map<String, String> choices = new LinkedHashMap<>();

        // Normalize line breaks and split into lines
        String[] lines = input.split("\\r?\\n");
        String currentSection = "";

        StringBuilder passageBuilder = new StringBuilder();
        StringBuilder questionBuilder = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            if (line.contains("Passage:")) {
                currentSection = "passage";
                continue;
            } else if ( line.contains("Question:")) {
                currentSection = "question";
                continue;
            } else if (line.contains("Choices:")) {
                currentSection = "choices";
                continue;
            } else if (line.contains("Answer:")) {
                currentSection = "answer";
                continue;
            }

            // Fill content based on section
            switch (currentSection) {
                case "passage":
                    passageBuilder.append(line).append("\n");
                    break;
                case "question":
                    questionBuilder.append(line).append(" ");
                    break;
                case "choices":
                    if (line.matches("^[A-D][).]\\s+.*")) {     //cater for both A) and A. styles
                        String key = line.substring(0, 1);
                        String value = line.substring(3).trim();
                        choices.put(key, value);
                    }
                    break;
                case "answer":
                    if (!line.isEmpty()) {
                        line = line.replaceAll("^\\*\\*(.*?)\\*\\*$", "$1").trim();

                        if (line.matches("^[A-D][).]\\s+.*")) {     //cater for both A) and A. styles
                            quizQuestion.setCorrectAnswer(line.substring(0, 1));
                        }
                    }
                    break;
            }
        }

        quizQuestion.setPassage(passageBuilder.toString().trim());
        quizQuestion.setQuestion(questionBuilder.toString().trim());
        quizQuestion.setAnswerChoices(choices);

       return quizQuestion;
    }
}
