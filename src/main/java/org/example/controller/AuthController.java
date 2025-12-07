package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginMode", "USER");
        return "login";
    }

    @GetMapping("/login/admin")
    public String adminLogin(Model model) {
        model.addAttribute("loginMode", "ADMIN");
        return "login";
    }
}

