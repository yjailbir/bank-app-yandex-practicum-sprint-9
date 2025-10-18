package ru.yjailbir.uiservice.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yjailbir.commonslib.dto.request.*;
import ru.yjailbir.commonslib.dto.response.UserAccountsResponseDto;
import ru.yjailbir.commonslib.dto.response.UserDataResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.util.AuthorizedHttpEntityFactory;

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
            ResponseEntity<UserDataResponseDto> userDataResponseEntity = restTemplate.postForEntity(
                    "http://accounts-service/user-data",
                    new EmptyRequestDtoWithToken(token), UserDataResponseDto.class
            );
            UserDataResponseDto userDataResponseDto = userDataResponseEntity.getBody();

            if (userDataResponseDto != null) {
                if (!userDataResponseEntity.getStatusCode().is2xxSuccessful() || !userDataResponseDto.status.equals("ok")) {
                    if (!model.containsAttribute("userAccountsErrors")) {
                        model.addAttribute("userAccountsErrors", List.of(userDataResponseDto.message));
                    }
                } else {
                    model.addAttribute("login", userDataResponseDto.login);
                    model.addAttribute("name", userDataResponseDto.name);
                    model.addAttribute("surname", userDataResponseDto.surname);
                    model.addAttribute("accounts", userDataResponseDto.accounts);
                }
            } else {
                model.addAttribute("login", "Сервис недоступен");
                model.addAttribute("name", "Сервис недоступен");
                model.addAttribute("surname", "Сервис недоступен");
                model.addAttribute("accounts", "Сервис недоступен");
            }

            ResponseEntity<UserAccountsResponseDto> userAccountsResponseEntity = restTemplate.postForEntity(
                    "http://accounts-service/active-accounts",
                    new EmptyRequestDtoWithToken(token), UserAccountsResponseDto.class
            );
            UserAccountsResponseDto userAccountsResponseDto = userAccountsResponseEntity.getBody();

            if (userAccountsResponseDto != null) {
                if (
                        !userAccountsResponseEntity.getStatusCode().is2xxSuccessful() ||
                                !userAccountsResponseDto.status.equals("ok")
                ) {
                    if (!model.containsAttribute("userAccountsErrors")) {
                        model.addAttribute("cashErrors", List.of(userAccountsResponseDto.message));
                    }
                } else {
                    model.addAttribute("currency", userAccountsResponseDto.accounts);
                }
            } else {
                model.addAttribute("currency", "Сервис недоступен");
            }
        }

        return "main";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @ModelAttribute PasswordChangeRequestDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!dto.password().equals(dto.confirmPassword())) {
            redirectAttributes.addFlashAttribute("passwordErrors", List.of("Пароли не совпадают"));
            return "redirect:/bank";
        } else {
            String token = session.getAttribute("JWT_TOKEN").toString();
            if (token != null) {
                ResponseEntity<MessageResponseDto> responseEntity = restTemplate.postForEntity(
                        "http://accounts-service/change-password",
                        new PasswordChangeRequestDtoWithToken(dto.password(), token), MessageResponseDto.class
                );
                MessageResponseDto messageResponseDto = responseEntity.getBody();

                if (messageResponseDto != null) {
                    if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                        redirectAttributes.addFlashAttribute("passwordErrors", List.of(messageResponseDto.message()));
                    }
                } else {
                    redirectAttributes.addFlashAttribute("passwordErrors", List.of("Сервис недоступен"));
                }
                return "redirect:/bank";
            } else {
                return "redirect:/auth/login";
            }
        }
    }

    @PostMapping("/edit")
    public String edit(
            @ModelAttribute UserEditRequestDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String token = session.getAttribute("JWT_TOKEN").toString();
        if (token != null) {
            ResponseEntity<MessageResponseDto> responseEntity = restTemplate.postForEntity(
                    "http://accounts-service/edit",
                    new UserEditRequestDtoWithToken(dto.name(), dto.surname(), dto.activeAccounts(), token),
                    MessageResponseDto.class
            );
            MessageResponseDto messageResponseDto = responseEntity.getBody();

            if (messageResponseDto != null) {
                if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                    redirectAttributes.addFlashAttribute("userAccountsErrors", List.of(messageResponseDto.message()));
                }
            } else {
                redirectAttributes.addFlashAttribute("userAccountsErrors", List.of("Сервис недоступен"));
            }
            return "redirect:/bank";
        } else {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/cash")
    public String cashOperation(
            @ModelAttribute CashRequestDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (dto.currency() == null) {
            redirectAttributes.addFlashAttribute("cashErrors", List.of("Нет ни одного открытого счёта!"));
            return "redirect:/bank";
        } else {
            String token = session.getAttribute("JWT_TOKEN").toString();

            if (token != null) {
                HttpEntity<CashRequestDtoWithToken> cashRequestEntity =
                        new AuthorizedHttpEntityFactory<CashRequestDtoWithToken>()
                                .createHttpEntityWithToken(new CashRequestDtoWithToken(
                                        dto.currency(), dto.value(), dto.action(), token), token
                                );
                ResponseEntity<MessageResponseDto> cashResponseEntity = restTemplate.postForEntity(
                        "http://cash-service/operate", cashRequestEntity, MessageResponseDto.class
                );
                MessageResponseDto cashResponseDto = cashResponseEntity.getBody();

                if (cashResponseDto != null) {
                    if (!cashResponseEntity.getStatusCode().is2xxSuccessful() || !cashResponseDto.status().equals("ok")) {
                        redirectAttributes.addFlashAttribute(
                                "cashErrors", List.of(cashResponseDto.message())
                        );
                    }
                } else {
                    redirectAttributes.addFlashAttribute("cashErrors", "Сервис недоступен");
                }

                return "redirect:/bank";
            } else {
                return "redirect:/auth/login";
            }
        }
    }
}
