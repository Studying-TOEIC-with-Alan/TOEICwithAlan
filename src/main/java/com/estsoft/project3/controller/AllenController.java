package com.estsoft.project3.controller;

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
    public ResponseEntity<?> askQuestion(@RequestParam String category, @RequestParam String inputText, HttpSession httpSession) {
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        String content = "";

        if (Objects.equals(category, "문법")) {
            content += "TOEIC 시험에 자주 나오는 문법 항목 5가지를 중요도 순으로 정리해서 간단한 설명과 함께 알려줘";
        } else if (Objects.equals(category, "문장 체크")) {
            content += "다음 영어 문장을 확인해 주세요.\n" +
                    "문장이 맞는지 틀린지 알려주세요.\n" +
                    "틀렸다면, 올바른 문장을 알려주세요.\n" +
                    "어떤 문법적 문제가 있었는지 한국어로 자세히 설명해 주세요.\n" +
                    "문장: " + inputText;
        } else if (Objects.equals(category, "어휘 목록")) {
            content += "다음 주제와 관련된 영어 단어 5개를 무작위로 선택해서 알려주세요.\n" +
                    "각 단어에 대해 다음을 포함해 주세요:\n" +
                    "1. 품사\n" +
                    "2. 한국어 뜻\n" +
                    "주제: " + inputText;
        } else if (Objects.equals(category, "어휘 설명")) {
            content += "다음 영어 단어를 한국어로 자세히 설명해 주세요.\n" +
                    "1. 품사\n" +
                    "2. 의미 (자세히)\n" +
                    "3. 비슷한 단어와 차이점 (있다면)\n" +
                    "4. 예문 2~3개 (영어 + 한국어 해석)\n" +
                    "단어: " + inputText;
        } else if (Objects.equals(category, "일기 퀴즈")) {
            content += "TOEIC Reading " + inputText + " 예시 문제를 만들어 주세요. " +
                    "문제는 반드시 1개만 생성해 주세요. " +
                    "지문, 질문, 선택지, 정답을 아래 형식으로 명확히 구분해 주세요:\n\n" +
                    "지문:\n\n" +
                    "질문:\n\n" +
                    "선택지:\n\n" +
                    "정답:";
        }

        String raw = allenApiService.getAnswer(content);

        //Save allen request and result info into allen table
        AllenRequest allenrequest = new AllenRequest();
        allenrequest.setUserId(sessionUser.getUserId());
        allenrequest.setCategory(category);
        allenrequest.setInputText(inputText);
        allenrequest.setSummary(raw);
        allenService.insertAllen(allenrequest);

        if (Objects.equals(category, "일기 퀴즈")) {
            //Parse toeic question to split passage, question, choices, and correct answer
            QuizQuestion quiz = toeicParserService.parse(raw);

            quiz.setPassage(convertMarkdownToHtml(quiz.getPassage()));

            return ResponseEntity.ok(quiz); // return as JSON
        } else {
            // Markdown to HTML for other categories
            String formattedResponse = raw.replace("\n", "<br>");   //replace \n to HTML enter

            return ResponseEntity.ok(convertMarkdownToHtml(formattedResponse));
        }
    }

    public String convertMarkdownToHtml(String markdown) {
        String trimmedMarkdown = markdown.trim();
        Parser parser = Parser.builder().build();
        Node document = parser.parse(trimmedMarkdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    @DeleteMapping("/api/allen")
    public ResponseEntity<Void> resetState () {
        allenApiService.resetState();
        return ResponseEntity.ok().build();
    }

}
