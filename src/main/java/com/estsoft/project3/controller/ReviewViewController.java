package com.estsoft.project3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewViewController {

    @GetMapping("/reviews/new")
    public String showCreateReviewForm() {

        return "review";
    }
}