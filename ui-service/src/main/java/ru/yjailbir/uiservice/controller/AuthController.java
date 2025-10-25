package ru.yjailbir.uiservice.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yjailbir.commonslib.dto.request.LoginRequestDto;
import ru.yjailbir.commonslib.dto.request.RegisterRequestDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.uiservice.service.AuthService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        if (!model.containsAttribute("errors")) {
            model.addAttribute("errors", Collections.emptyList());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequestDto dto, Model model) {
        ResponseEntity<MessageResponseDto> responseEntity = authService.register(dto);
        MessageResponseDto messageResponseDto = responseEntity.getBody();

        if (messageResponseDto != null) {
            if (responseEntity.getStatusCode().is2xxSuccessful() && messageResponseDto.status().equals("ok")) {
                return "redirect:/auth/login";
            } else {
                model.addAttribute("errors", List.of(messageResponseDto.message()));
                return "register";
            }
        } else {
            model.addAttribute("errors", List.of("Сервис недоступен"));
            return "register";
        }
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        if (!model.containsAttribute("errors")) {
            model.addAttribute("errors", Collections.emptyList());
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequestDto dto, HttpSession session, Model model) {
        ResponseEntity<MessageResponseDto> responseEntity = authService.login(dto);
        MessageResponseDto messageResponseDto = responseEntity.getBody();

        if (messageResponseDto != null) {
            if (responseEntity.getStatusCode().is2xxSuccessful() && messageResponseDto.status().equals("ok")) {
                session.setAttribute("JWT_TOKEN", messageResponseDto.message());
                return "redirect:/bank";
            } else {
                model.addAttribute("errors", List.of(messageResponseDto.message()));
                return "login";
            }
        } else {
            model.addAttribute("errors", List.of("Сервис недоступен"));
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }
}
