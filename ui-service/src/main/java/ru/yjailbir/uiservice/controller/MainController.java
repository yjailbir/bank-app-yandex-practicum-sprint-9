package ru.yjailbir.uiservice.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yjailbir.commonslib.dto.request.*;
import ru.yjailbir.commonslib.dto.response.AllUserLoginsResponseDto;
import ru.yjailbir.commonslib.dto.response.UserAccountsResponseDto;
import ru.yjailbir.commonslib.dto.response.UserDataResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.uiservice.client.AccountsServiceClient;
import ru.yjailbir.uiservice.client.CashServiceClient;
import ru.yjailbir.uiservice.client.TransferServiceClient;

import java.util.List;

@Controller
@RequestMapping("/bank")
public class MainController {
    private final AccountsServiceClient accountsServiceClient;
    private final CashServiceClient cashServiceClient;
    private final TransferServiceClient transferServiceClient;

    @Autowired
    public MainController(
            AccountsServiceClient accountsServiceClient,
            CashServiceClient cashServiceClient,
            TransferServiceClient transferServiceClient
    ) {
        this.accountsServiceClient = accountsServiceClient;
        this.cashServiceClient = cashServiceClient;
        this.transferServiceClient = transferServiceClient;
    }

    @GetMapping
    public String mainPage(HttpSession session, Model model) {
        if (session.getAttribute("JWT_TOKEN") == null) {
            return "redirect:/auth/login";
        } else {
            String token = session.getAttribute("JWT_TOKEN").toString();
            ResponseEntity<UserDataResponseDto> userDataResponseEntity = accountsServiceClient.getUserData(token);
            UserDataResponseDto userDataResponseDto = userDataResponseEntity.getBody();

            assert userDataResponseDto != null;
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

            ResponseEntity<UserAccountsResponseDto> userAccountsResponseEntity =
                    accountsServiceClient.getUserAccounts(token);
            UserAccountsResponseDto userAccountsResponseDto = userAccountsResponseEntity.getBody();

            assert userAccountsResponseDto != null;
            if (
                    !userAccountsResponseEntity.getStatusCode().is2xxSuccessful() ||
                            !userAccountsResponseDto.status.equals("ok")
            ) {
                if (!model.containsAttribute("userAccountsErrors")) {
                    model.addAttribute("cashErrors", List.of(userAccountsResponseDto.message));
                }
            } else {
                model.addAttribute("currency", userAccountsResponseDto.accounts);
                model.addAttribute("otherCurrency", userAccountsResponseDto.accounts);
            }


            ResponseEntity<AllUserLoginsResponseDto> allUserLoginsResponseEntity =
                    accountsServiceClient.getAllUserLogins(token);
            AllUserLoginsResponseDto allUserLoginsResponseDto = allUserLoginsResponseEntity.getBody();

            assert allUserLoginsResponseDto != null;
            if (
                    !allUserLoginsResponseEntity.getStatusCode().is2xxSuccessful() ||
                            !allUserLoginsResponseDto.status.equals("ok")
            ) {
                model.addAttribute("users", List.of(allUserLoginsResponseDto.message));
            } else {
                List<String> logins = allUserLoginsResponseDto.logins.stream().filter(
                        x -> !x.equals(userDataResponseDto.login)
                ).toList();
                model.addAttribute("users", logins);
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
                ResponseEntity<MessageResponseDto> responseEntity = accountsServiceClient.changePassword(token, dto);
                MessageResponseDto messageResponseDto = responseEntity.getBody();

                assert messageResponseDto != null;
                if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                    redirectAttributes.addFlashAttribute("passwordErrors", List.of(messageResponseDto.message()));
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
            ResponseEntity<MessageResponseDto> responseEntity = accountsServiceClient.editUser(token, dto);
            MessageResponseDto messageResponseDto = responseEntity.getBody();

            assert messageResponseDto != null;
            if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                redirectAttributes.addFlashAttribute("userAccountsErrors", List.of(messageResponseDto.message()));
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

                ResponseEntity<MessageResponseDto> cashResponseEntity = cashServiceClient.doCash(token, dto);
                MessageResponseDto cashResponseDto = cashResponseEntity.getBody();

                assert cashResponseDto != null;
                if (!cashResponseEntity.getStatusCode().is2xxSuccessful() || !cashResponseDto.status().equals("ok")) {
                    redirectAttributes.addFlashAttribute(
                            "cashErrors", List.of(cashResponseDto.message())
                    );
                }

                return "redirect:/bank";
            } else {
                return "redirect:/auth/login";
            }
        }
    }

    @PostMapping("/transfer")
    public String transfer(
            @ModelAttribute TransferRequestDto dto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (dto.fromCurrency() == null) {
            redirectAttributes.addFlashAttribute("cashErrors", List.of("Нет ни одного открытого счёта!"));
            return "redirect:/bank";
        } else {
            String token = session.getAttribute("JWT_TOKEN").toString();

            if (token != null) {

                ResponseEntity<MessageResponseDto> transferResponseEntity = transferServiceClient.doTransfer(token, dto);
                MessageResponseDto transferResponseDto = transferResponseEntity.getBody();

                assert transferResponseDto != null;
                if (!transferResponseEntity.getStatusCode().is2xxSuccessful() || !transferResponseDto.status().equals("ok")) {
                    redirectAttributes.addFlashAttribute(
                            "transferErrors", List.of(transferResponseDto.message())
                    );
                }

                return "redirect:/bank";
            } else {
                return "redirect:/auth/login";
            }
        }
    }
}
