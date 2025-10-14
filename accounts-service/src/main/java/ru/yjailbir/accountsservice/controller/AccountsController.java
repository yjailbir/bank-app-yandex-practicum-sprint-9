package ru.yjailbir.accountsservice.controller;

import org.springframework.web.bind.annotation.*;
import ru.yjailbir.commonservice.dto.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import ru.yjailbir.commonservice.dto.response.MessageResponseDto;
import ru.yjailbir.accountsservice.service.UserService;
import ru.yjailbir.commonservice.dto.response.UserAccountsResponseDto;
import ru.yjailbir.commonservice.dto.response.UserDataResponseDto;

@RestController
public class AccountsController {
    private final UserService userService;

    @Autowired
    public AccountsController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDto> registerAccount(@RequestBody RegisterRequestDto dto) {
        try {
            userService.saveNewUser(dto);
            return ResponseEntity.ok(new MessageResponseDto("ok", "пользователь зарегистрирован"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDto> login(@RequestBody LoginRequestDto dto) {
        try {
            String token = userService.loginUser(dto);
            return ResponseEntity.ok(new MessageResponseDto("ok", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public String validateToken(@RequestBody EmptyRequestDtoWithToken dto) {
        return userService.validateToken(dto.token());
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponseDto> changePassword(@RequestBody PasswordChangeRequestDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", tokenValidationResult));
        } else {
            try {
                userService.updateUserPassword(dto);
                return ResponseEntity.ok(new MessageResponseDto("ok", ""));
            } catch (IllegalArgumentException e) {
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
                return ResponseEntity.ok(new MessageResponseDto("ok", ""));
            } catch (IllegalArgumentException e) {
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
                return ResponseEntity.badRequest().body(new MessageResponseDto("error", e.getMessage()));
            }
        }
    }
}
