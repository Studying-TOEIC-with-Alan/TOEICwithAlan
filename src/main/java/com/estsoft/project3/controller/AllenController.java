package com.estsoft.project3.controller;

import com.estsoft.project3.domain.Allen;
import com.estsoft.project3.dto.AllenRequest;
import com.estsoft.project3.dto.QuizQuestion;
import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.service.AllenApiService;
import com.estsoft.project3.service.AllenService;
import com.estsoft.project3.service.ToeicParserService;
import jakarta.servlet.http.HttpSession;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
public class AllenController {
    @Autowired
    private AllenApiService allenApiService;

    @Autowired
    private AllenService allenService;

    @Autowired
    private ToeicParserService toeicParserService;

    @PostMapping("/api/allen")
    public ResponseEntity<Void> insertAllen (@RequestBody AllenRequest request) {
        allenService.insertAllen(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/resetAllen")
    public ResponseEntity<Void> resetState () {
        allenApiService.resetState();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/askAllen")
    public ResponseEntity<?> askAllen(@RequestParam String category, @RequestParam String inputText, HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        Long userId = sessionUser.getUserId();

        // Set input content to allen api based on selected category
        String content = SetAllenInputContent(category, inputText, userId);

        String raw = allenApiService.getAnswer(content);

        if (Objects.equals(category, "일기 퀴즈") || Objects.equals(category, "듣기 퀴즈")) {
            //Parse toeic question to split passage, question, choices, and correct answer
            QuizQuestion quiz = toeicParserService.parse(raw);

            quiz.setAllenInputText(inputText);

            return ResponseEntity.ok(quiz); // return as JSON
        } else {
            // Markdown to HTML for other categories
            String formattedResponse = raw.replace("\n", "<br>");   //replace \n to HTML enter

            return ResponseEntity.ok(convertMarkdownToHtml(formattedResponse));
        }
    }

    private String SetAllenInputContent(String category, String inputText, Long userId) {
        String content = "";

        if (Objects.equals(category, "문법")) {
            content = "TOEIC 시험에 자주 나오는 문법 항목 5가지를 중요도 순으로 정리해서 간단한 설명과 함께 알려줘";
        } else if (Objects.equals(category, "문장 체크")) {
            content = "다음 영어 문장을 확인해 주세요.\n" +
                    "문장이 맞는지 틀린지 알려주세요.\n" +
                    "틀렸다면, 올바른 문장을 알려주세요.\n" +
                    "어떤 문법적 문제가 있었는지 한국어로 자세히 설명해 주세요.\n" +
                    "문장: " + inputText;
        } else if (Objects.equals(category, "어휘 목록")) {
            content = "다음 주제와 관련된 영어 단어 5개를 무작위로 선택해서 알려주세요.\n" +
                    "각 단어에 대해 다음을 포함해 주세요:\n" +
                    "1. 품사\n" +
                    "2. 한국어 뜻\n" +
                    "주제: " + inputText;
        } else if (Objects.equals(category, "어휘 설명")) {
            content = "다음 영어 단어를 한국어로 자세히 설명해 주세요.\n" +
                    "1. 품사\n" +
                    "2. 의미 (자세히)\n" +
                    "3. 비슷한 단어와 차이점 (있다면)\n" +
                    "4. 예문 2~3개 (영어 + 한국어 해석)\n" +
                    "단어: " + inputText;
        } else if (Objects.equals(category, "일기 퀴즈")) {
            inputText = getNextQuestionInput (userId, category, inputText);

            content = "Generate one TOEIC Reading question for " + inputText + "\n\n" +
                    "Please provide the output with these clearly labeled sections:\n";

            if (inputText.contains("Part 5")) {
                content += "Passage: (the incomplete sentence with a blank)\n\n" +
                        "Question: (the question asking which option best completes the sentence)\n\n";
            } else if (inputText.contains("Part 6")) {
                content += "Passage: (a short passage with one or more blanks)\n\n" +
                        "Question: (which option best completes each blank)\n\n";
            } else if (inputText.contains("Part 7")) {
                content += "Passage: (the full reading passage or text)\n\n" +
                        "Question: (the comprehension question)\n\n";
            }

            content += "Choices: (list options A), B), C), D))\n\n" +
                    "Answer: (the correct choice)";
        } else if (Objects.equals(category, "듣기 퀴즈")) {
            inputText = getNextQuestionInput (userId, category, inputText);

            content = "Generate one TOEIC Listening question for " + inputText + "\n\n" +
                    "For each question, please provide these clearly labeled sections:\n";

            if (inputText.contains("Part 2")) {
                content += "Passage: (the spoken question prompt)\n\n" +
                        "Question: (a comprehension question about the passage)\n\n";
            } else if (inputText.contains("Part 3")){
                content = "Passage: (the conversation transcript)\n\n" +
                        "Question: (the question asked)\n\n";
            } else {
                content = "Passage: (the short talk)\n\n" +
                        "Question: (the question asked)\n\n";
            }

            content += "Choices: (list options A), B), C), D))\n\n" +
                    "Answer: (the correct choice)";
        }

        return content;
    }

    //TOEIC Question start number for each part
    private static final Map<String, Integer> partStart = Map.of(
            "Part 2", 1,
            "Part 3", 26,
            "Part 4", 65,
            "Part 5", 95,
            "Part 6", 125,
            "Part 7", 141
    );

    //TOEIC Question end number for each part
    private static final Map<String, Integer> partEnd = Map.of(
            "Part 2", 25,
            "Part 3", 64,
            "Part 4", 94,
            "Part 5", 124,
            "Part 6", 140,
            "Part 7", 194
    );

    public String getNextQuestionInput (Long userId, String category, String inputText) {
        Allen latestAllen = allenService.GetLastAllenByUserAndCatAndInput(userId, category, inputText);
        String inputKeyword = "";

        if (latestAllen == null) {
            inputKeyword = inputText + " Question 1 Day 1";
        } else {
            String[] inputItems = latestAllen.getInputText().split(" ");
            String part = inputItems[0] + " " + inputItems[1];
            int lastQuestionNo = Integer.parseInt(inputItems[3]);
            int lastDayNo = Integer.parseInt(inputItems[5]);

            int nextQuestionNo = lastQuestionNo + 1;
            int nextDayNo = lastDayNo;

            int startNo = partStart.get(part);
            int endNo = partEnd.get(part);

            //When no more question for the part, back to first question of the part and change to next day
            if (nextQuestionNo > endNo) {
                nextQuestionNo = startNo;
                nextDayNo++;
            }

            inputKeyword = part + " Question " + nextQuestionNo + " Day " + nextDayNo;
        }

        return inputKeyword;
    }

    public String convertMarkdownToHtml(String markdown) {
        String trimmedMarkdown = markdown.trim();
        Parser parser = Parser.builder().build();
        Node document = parser.parse(trimmedMarkdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

}
