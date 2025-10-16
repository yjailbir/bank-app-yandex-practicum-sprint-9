package ru.yjailbir.blockerservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

import java.util.Random;

@RestController
public class BlockerController {
    @PostMapping("/check-cash-operation")
    public ResponseEntity<MessageResponseDto> checkCashOperation(@RequestBody CashRequestDtoWithToken dto){
        if(new Random().nextInt(11) % 5 == 0) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", "Операция заблокирована"));
        } else {
            return ResponseEntity.ok(new MessageResponseDto("ok", ""));
        }
    }
}
