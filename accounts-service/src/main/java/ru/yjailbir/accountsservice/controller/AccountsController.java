package ru.yjailbir.accountsservice.controller;

import dto.request.LoginRequestDto;
import dto.request.RegisterRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import dto.response.ResponseDto;
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
            return ResponseEntity.ok(new ResponseDto("ok", "user registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseDto("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(@RequestBody LoginRequestDto dto) {
        //todo сделать нормально
        return ResponseEntity.ok(new ResponseDto("ok", userService.loginUser(dto)));
    }
}
