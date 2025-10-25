package ru.yjailbir.blockerservice.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

import java.util.Random;

@Service
public class BlockerService {
    private final Random random = new Random();

    public ResponseEntity<MessageResponseDto> checkOperation() {
        if(random.nextInt(11) % 5 == 0) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", "Операция заблокирована"));
        } else {
            return ResponseEntity.ok(new MessageResponseDto("ok", ""));
        }
    }
}
