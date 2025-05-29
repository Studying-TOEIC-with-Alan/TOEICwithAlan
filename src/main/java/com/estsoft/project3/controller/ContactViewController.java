package com.estsoft.project3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactViewController {

    @GetMapping("/contacts/new")
    public String showCreateContactForm() {

        return "contact";
    }
}
