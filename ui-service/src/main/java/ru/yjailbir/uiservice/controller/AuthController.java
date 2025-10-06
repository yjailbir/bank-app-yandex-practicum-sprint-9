package ru.yjailbir.uiservice.controller;

import dto.request.LoginRequestDto;
import dto.request.RegisterRequestDto;
import dto.response.ResponseDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final RestTemplate restTemplate;

    @Autowired
    public AuthController(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
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
        ResponseEntity<ResponseDto> responseEntity = restTemplate.postForEntity(
                "http://accounts-service/register", dto, ResponseDto.class
        );
        ResponseDto responseDto = responseEntity.getBody();

        if (responseDto != null) {
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseDto.status().equals("ok")) {
                return "redirect:/auth/login";
            } else {
                model.addAttribute("errors", List.of(responseDto.message()));
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
        ResponseEntity<ResponseDto> responseEntity = restTemplate.postForEntity(
                "http://accounts-service/login", dto, ResponseDto.class
        );
        ResponseDto responseDto = responseEntity.getBody();

        if (responseDto != null) {
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseDto.status().equals("ok")) {
                session.setAttribute("JWT_TOKEN", responseDto.message());
                return "redirect:/auth/test"; //todo убрать это
            } else {
                model.addAttribute("errors", List.of(responseDto.message()));
                return "login";
            }
        } else {
            model.addAttribute("errors", List.of("Сервис недоступен"));
            return "login";
        }
    }

    @GetMapping("/test")
    public String mainPage(HttpSession session, Model model) {
        if (session.getAttribute("JWT_TOKEN") == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("data", List.of(session.getAttribute("JWT_TOKEN")));
        return "test";
    }
}
