package ru.yjailbir.accountsservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import ru.yjailbir.commonslib.client.NotificationClient;
import ru.yjailbir.commonslib.dto.request.*;
import ru.yjailbir.commonslib.dto.response.AllUserLoginsResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.accountsservice.service.UserService;
import ru.yjailbir.commonslib.dto.response.UserAccountsResponseDto;
import ru.yjailbir.commonslib.dto.response.UserDataResponseDto;

@RestController
public class AccountsController {
    private final Logger logger = LoggerFactory.getLogger(AccountsController.class);

    private final UserService userService;
    private final NotificationClient notificationClient;

    @Autowired
    public AccountsController(UserService userService, NotificationClient notificationClient) {
        this.userService = userService;
        this.notificationClient = notificationClient;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDto> registerAccount(@RequestBody RegisterRequestDto dto) {
        try {
            userService.saveNewUser(dto);
            return ResponseEntity.ok(new MessageResponseDto("ok", "пользователь зарегистрирован"));
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDto> login(@RequestBody LoginRequestDto dto) {
        try {
            String token = userService.loginUser(dto);
            return ResponseEntity.ok(new MessageResponseDto("ok", token));
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestBody EmptyRequestDtoWithToken dto) {
        String result = userService.validateToken(dto.token());
        if (result.equals("ok")) {
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponseDto> changePassword(@RequestBody PasswordChangeRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", tokenValidationResult));
        } else {
            try {
                userService.updateUserPassword(dto);
                notificationClient.sendNotification(
                        "Пользователь " + userService.getLoginFromToken(dto.token()) + " поменял пароль"
                );
                return ResponseEntity.ok(new MessageResponseDto("ok", ""));
            } catch (IllegalArgumentException e) {
                logError(e);
                return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
            }
        }
    }

    @PostMapping("/user-data")
    public ResponseEntity<UserDataResponseDto> getUserData(@RequestBody EmptyRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new UserDataResponseDto("error", tokenValidationResult));
        } else {
            try {
                return ResponseEntity.ok(userService.getUserData(dto.token()));
            } catch (IllegalArgumentException e) {
                logError(e);
                return ResponseEntity.badRequest().body(new UserDataResponseDto("error", e.getMessage()));
            }
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<MessageResponseDto> editUser(@RequestBody UserEditRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", tokenValidationResult));
        } else {
            try {
                userService.updateUser(dto);
                notificationClient.sendNotification(
                        "Пользователь " + userService.getLoginFromToken(dto.token()) + " поменял данные пользователя"
                );
                return ResponseEntity.ok(new MessageResponseDto("ok", ""));
            } catch (IllegalArgumentException | IllegalStateException e) {
                logError(e);
                return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
            }
        }
    }

    @PostMapping("/active-accounts")
    public ResponseEntity<UserAccountsResponseDto> getActiveAccounts(@RequestBody EmptyRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new UserAccountsResponseDto("error", tokenValidationResult));
        } else {
            try {
                return ResponseEntity.ok(userService.getUserActiveAccounts(dto.token()));
            } catch (IllegalArgumentException e) {
                logError(e);
                return ResponseEntity.badRequest().body(new UserAccountsResponseDto("error", e.getMessage()));
            }
        }
    }

    @PostMapping("/cash")
    public ResponseEntity<MessageResponseDto> cashOperation(@RequestBody CashRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", tokenValidationResult));
        } else {
            try {
                userService.doCashOperation(dto);
                return ResponseEntity.ok(new MessageResponseDto("ok", ""));
            } catch (IllegalArgumentException e) {
                logError(e);
                return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
            }
        }
    }

    @PostMapping("/all-logins")
    public ResponseEntity<AllUserLoginsResponseDto> getAllLogins(@RequestBody EmptyRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new AllUserLoginsResponseDto("error", tokenValidationResult));
        } else {
            return ResponseEntity.ok(userService.getAllUserLogins());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<MessageResponseDto> transfer(@RequestBody ExchangedTransferDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", tokenValidationResult));
        } else {
            try {
                userService.doTransfer(dto);
                return ResponseEntity.ok(new MessageResponseDto("ok", ""));
            } catch (IllegalArgumentException e) {
                logError(e);
                return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
            }
        }
    }

    private void logError(Exception e) {
        logger.error("{}", e.getMessage());
    }
}
