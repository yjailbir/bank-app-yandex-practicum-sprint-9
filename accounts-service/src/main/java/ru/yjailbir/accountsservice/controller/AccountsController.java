package ru.yjailbir.accountsservice.controller;

import jakarta.servlet.http.HttpSession;
import ru.yjailbir.commonservice.dto.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonservice.dto.response.ResponseDto;
import ru.yjailbir.accountsservice.service.UserService;

@RestController
public class AccountsController {
    private final UserService userService;

    @Autowired
    public AccountsController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> registerAccount(@RequestBody RegisterRequestDto dto) {
        try {
            userService.saveNewUser(dto);
            return ResponseEntity.ok(new ResponseDto("ok", "пользователь зарегистрирован"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestBody LoginRequestDto dto) {
        try {
            String token = userService.loginUser(dto);
            return ResponseEntity.ok(new ResponseDto("ok", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public String validateToken(@RequestBody TokenValidationRequestDto dto) {
        return userService.validateToken(dto.token());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseDto> changePassword(@RequestBody PasswordChangeDtoWithToken dto) {
        String tokenValidationResult = userService.validateToken(dto.token());
        if (!tokenValidationResult.equals("ok")) {
            return ResponseEntity.badRequest().body(new ResponseDto("error", tokenValidationResult));
        } else {
            try {
                userService.updateUserPassword(dto);
                return ResponseEntity.ok(new ResponseDto("ok", ""));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(new ResponseDto("error", e.getMessage()));
            }
        }
    }
}
