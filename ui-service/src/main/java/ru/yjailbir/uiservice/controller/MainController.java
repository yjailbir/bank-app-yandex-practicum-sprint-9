package ru.yjailbir.uiservice.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bank")
public class MainController {

    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model) {
        if (session.getAttribute("JWT_TOKEN") == null) {
            return "redirect:/auth/login";
        }

        return "main";
    }
}
