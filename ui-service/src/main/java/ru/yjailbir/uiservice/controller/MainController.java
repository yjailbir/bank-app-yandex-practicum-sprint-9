package ru.yjailbir.uiservice.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonservice.dto.request.PasswordChangeDto;
import ru.yjailbir.commonservice.dto.request.PasswordChangeDtoWithToken;
import ru.yjailbir.commonservice.dto.request.TokenDto;
import ru.yjailbir.commonservice.dto.response.UserDataResponseDto;
import ru.yjailbir.commonservice.dto.response.MessageResponseDto;

import java.util.List;

@Controller
@RequestMapping("/bank")
public class MainController {
    private final RestTemplate restTemplate;

    @Autowired
    public MainController(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String mainPage(HttpSession session, Model model) {
        if (session.getAttribute("JWT_TOKEN") == null) {
            return "redirect:/auth/login";
        } else {
            String token = session.getAttribute("JWT_TOKEN").toString();
            ResponseEntity<UserDataResponseDto> responseEntity = restTemplate.postForEntity(
                    "http://accounts-service/user-data",
                    new TokenDto(token), UserDataResponseDto.class
            );
            UserDataResponseDto userDataResponseDto = responseEntity.getBody();

            if (userDataResponseDto != null) {
                if (!responseEntity.getStatusCode().is2xxSuccessful() || !userDataResponseDto.status.equals("ok")) {
                    model.addAttribute("userAccountsErrors", List.of(userDataResponseDto.message));
                } else {
                    model.addAttribute("login", userDataResponseDto.login);
                    model.addAttribute("name", userDataResponseDto.name);
                    model.addAttribute("surname", userDataResponseDto.surname);
                }
            } else {
                model.addAttribute("login", "Сервис недоступен");
                model.addAttribute("name", "Сервис недоступен");
                model.addAttribute("surname", "Сервис недоступен");
            }
        }

        return "main";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute PasswordChangeDto dto, HttpSession session, Model model) {
        if (!dto.password().equals(dto.confirmPassword())) {
            model.addAttribute("passwordErrors", List.of("Пароли не совпадают"));
            return "main";
        } else {
            String token = session.getAttribute("JWT_TOKEN").toString();
            if (token != null) {

                ResponseEntity<MessageResponseDto> responseEntity = restTemplate.postForEntity(
                        "http://accounts-service/change-password",
                        new PasswordChangeDtoWithToken(dto.password(), token), MessageResponseDto.class
                );
                MessageResponseDto messageResponseDto = responseEntity.getBody();

                if (messageResponseDto != null) {
                    if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                        model.addAttribute("passwordErrors", List.of(messageResponseDto.message()));
                    }
                } else {
                    model.addAttribute("passwordErrors", List.of("Сервис недоступен"));
                }
                return "main";
            } else {
                return "redirect:/auth/login";
            }
        }
    }

}
