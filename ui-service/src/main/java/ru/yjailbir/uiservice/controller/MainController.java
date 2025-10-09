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
import ru.yjailbir.commonservice.dto.response.ResponseDto;

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

                ResponseEntity<ResponseDto> responseEntity = restTemplate.postForEntity(
                        "http://accounts-service/change-password",
                        new PasswordChangeDtoWithToken(dto.password(), token), ResponseDto.class
                );
                ResponseDto responseDto = responseEntity.getBody();

                if (responseDto != null) {
                    if (!responseEntity.getStatusCode().is2xxSuccessful() || !responseDto.status().equals("ok")) {
                        model.addAttribute("passwordErrors", List.of(responseDto.message()));
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
