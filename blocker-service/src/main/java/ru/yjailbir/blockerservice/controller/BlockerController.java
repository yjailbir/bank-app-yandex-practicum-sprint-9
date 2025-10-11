package ru.yjailbir.blockerservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonservice.dto.response.MessageResponseDto;

@RestController
public class BlockerController {
    @PostMapping("check-cash-operation")
    public ResponseEntity<MessageResponseDto> checkCashOperation(){

    }
}
