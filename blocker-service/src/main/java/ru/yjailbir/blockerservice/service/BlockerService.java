package ru.yjailbir.blockerservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

import java.util.Random;

@Service
public class BlockerService {
    private final Logger logger = LoggerFactory.getLogger(BlockerService.class);

    private final Random random = new Random();
    private final MeterRegistry meterRegistry;

    @Autowired
    public BlockerService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public ResponseEntity<MessageResponseDto> checkOperation() {
        if(random.nextInt(11) % 5 == 0) {
            meterRegistry.counter("operations_blocked_total").increment();
            logger.info("Operation blocked");
            return ResponseEntity.badRequest().body(new MessageResponseDto("error", "Операция заблокирована"));
        } else {
            return ResponseEntity.ok(new MessageResponseDto("ok", ""));
        }
    }
}
