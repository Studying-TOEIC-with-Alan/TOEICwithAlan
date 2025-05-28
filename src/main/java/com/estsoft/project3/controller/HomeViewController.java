package com.estsoft.project3.controller;

import com.estsoft.project3.service.AllenApiService;
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

    public HomeViewController(AllenApiService allenApiService) {
        this.allenApiService = allenApiService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        Long userId = 1L;   //to be replaced to real one
        model.addAttribute("userId", userId);   //to be replaced to real one
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
