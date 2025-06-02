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

        System.out.println("닉네임: " + sessionUser.getNickname());
        System.out.println("유저 ID: " + sessionUser.getUserId());
        System.out.println("점수: " + sessionUser.getScore());
        System.out.println("역할: " + sessionUser.getRole());
        System.out.println("등급: " + sessionUser.getGrade());

        model.addAttribute("userId", sessionUser.getUserId());
        model.addAttribute("nickname", sessionUser.getNickname());
        model.addAttribute("role", sessionUser.getRole());
        model.addAttribute("score", sessionUser.getScore());
        model.addAttribute("grade", sessionUser.getGrade());
        model.addAttribute("categories", List.of("문법", "어휘"));

        return "index";
    }

    @PostMapping("/question")
    public String askQuestion(@RequestParam String category, @RequestParam String inputText, RedirectAttributes redirectAttributes) {
        String content = "";

        if (Objects.equals(category, "문법")) {
            content += "문장 맞는 지 확인하고 틀리면 맞는 문장 하고 설명 요약(최대 1000자), 문장 : " + inputText;
        } else if (Objects.equals(category, "어휘")) {
            content += inputText + " 관련있는 어휘 예시 하고 의미 (최대 1000자)";
        }

        String answer = allenApiService.getAnswer(content);

        String formattedAnswer = answer.replace("\n", "<br>");   //replace \n to HTML enter
        formattedAnswer = formattedAnswer.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");     //replace bold ** to HTML strong

        String htmlAnswer = allenApiService.convertMarkdownToHtml(formattedAnswer);

        redirectAttributes.addFlashAttribute("category", category);
        redirectAttributes.addFlashAttribute("answer", htmlAnswer);

        return "redirect:/";
    }

}
