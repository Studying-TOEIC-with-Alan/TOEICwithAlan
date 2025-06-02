package com.estsoft.project3.controller;

import com.estsoft.project3.dto.SessionUser;
import com.estsoft.project3.service.AllenApiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
public class HomeViewController {
    private final AllenApiService allenApiService;
    private final HttpSession httpSession;

    public HomeViewController(AllenApiService allenApiService, HttpSession httpSession) {
        this.allenApiService = allenApiService;
        this.httpSession = httpSession;
    }

    @GetMapping("/")
    public String showForm(Model model) {

        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");

        if (sessionUser == null) {
            return "redirect:/login";
        }

        if (sessionUser != null) {
            model.addAttribute("userId", sessionUser.getUserId());
            model.addAttribute("role", sessionUser.getRole());
            model.addAttribute("nickname", sessionUser.getNickname());
            model.addAttribute("score", sessionUser.getScore());
            model.addAttribute("grade", sessionUser.getGrade());
        }

        model.addAttribute("categories", List.of("문장 체크", "어휘 목록", "어휘 설명", "어휘 플래시 카드"));

        return "index";
    }

    @PostMapping("/question")
    public String askQuestion(@RequestParam String category, @RequestParam String inputText, RedirectAttributes redirectAttributes) {
        String content = "";

        if (Objects.equals(inputText.trim(), "")) {
            throw new RuntimeException("입력 없습니다");
        }

        if (Objects.equals(category, "문장 체크")) {
            content += "문장 맞는 지 확인하고 틀리면 맞는 문장 하고 설명 요약(최대 1000자), 문장 : " + inputText;
        } else if (Objects.equals(category, "어휘 플래시 카드")) {
            content += inputText + " 영어 어휘 하고 그 어휘의뜻 (최대 1000자)";
        } else if (Objects.equals(category, "어휘 목록")) {
            content += inputText + " 관련있는 어휘 뜻하고 문장 예시 (최대 1000자)";
        } else if (Objects.equals(category, "어휘 설명")) {
            content += inputText + " 영어에 한국어로 번역하고 문장 예시 (최대 1000자)";
        }

        String answer = allenApiService.getAnswer(content);

        //Format answer from Allen API
        String formattedAnswer = answer.replace("\n", "<br>");   //replace \n to HTML enter
        formattedAnswer = formattedAnswer.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");     //replace bold ** to HTML strong

        String htmlAnswer = allenApiService.convertMarkdownToHtml(formattedAnswer);

        redirectAttributes.addFlashAttribute("category", category);
        redirectAttributes.addFlashAttribute("answer", htmlAnswer);

        return "redirect:/";
    }

}
