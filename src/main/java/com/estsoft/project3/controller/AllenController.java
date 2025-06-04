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

    @PostMapping("/api/askAllen")
    public ResponseEntity<?> askQuestion(@RequestParam String category, @RequestParam String inputText, HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        Long userId = sessionUser.getUserId();

        // Set input content to allen api based on selected category
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
            content = "TOEIC Reading " + inputText + " 예시 문제를 만들어 주세요. " +
                    "문제는 반드시 1개만 생성해 주세요. " +
                    "지문, 질문, 선택지 4개 (A, B, C, D), 정답을 아래 형식으로 명확히 구분해 주세요:\n\n" +
                    "지문:\n\n" +
                    "질문:\n\n" +
                    "선택지:\n\n" +
                    "정답:";
        } else if (Objects.equals(category, "듣기 퀴즈")) {
            inputText = getNextQuestionInput (userId, category, inputText);

            if (inputText.contains("Part 2")) {
                content = "TOEIC Listening " + inputText + " 예시 문제를 만들어 주세요.\n\n" +
                        "파트 2는 한 사람의 질문 또는 말에 대한 응답을 고르는 형식입니다.\n" +
                        "스크립트는 질문 또는 말 한 문장만 작성해 주세요.\n\n" +
                        "스크립트:\n\n" +
                        "질문:\n\n" +
                        "선택지:\n\n" +
                        "정답:";
            } else if (inputText.contains("Part 3") || inputText.contains("Part 4")) {
                content = "TOEIC Listening " + inputText + " 예시 문제를 만들어 주세요.\n\n" +
                        "파트 3과 4는 두 명 이상의 대화 또는 설명문 형식입니다.\n" +
                        "스크립트에는 반드시 2명 이상의 화자가 포함되어야 하며, 실제 대화처럼 자연스럽게 작성해 주세요.\n\n" +
                        "스크립트:\n\n" +
                        "질문:\n\n" +
                        "선택지:\n\n" +
                        "정답:";
            }
        }

        String raw = allenApiService.getAnswer(content);

        if (Objects.equals(category, "일기 퀴즈") || Objects.equals(category, "듣기 퀴즈")) {
            //Parse toeic question to split passage, question, choices, and correct answer
            QuizQuestion quiz = toeicParserService.parse(raw);

            quiz.setPassage(convertMarkdownToHtml(quiz.getPassage()));
            quiz.setAllenInputText(inputText);

            return ResponseEntity.ok(quiz); // return as JSON
        } else {
            // Markdown to HTML for other categories
            String formattedResponse = raw.replace("\n", "<br>");   //replace \n to HTML enter

            return ResponseEntity.ok(convertMarkdownToHtml(formattedResponse));
        }
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

    @PostMapping("/api/allen")
    public ResponseEntity<Void> insertAllen (@RequestBody AllenRequest request) {
        allenService.insertAllen(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/allen")
    public ResponseEntity<Void> resetState () {
        allenApiService.resetState();
        return ResponseEntity.ok().build();
    }

}
