package ru.yjailbir.transferservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.ExchangedTransferDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

@Service
public class AccountsServiceClient {
    private final RestTemplate restTemplate;

    @Autowired
    public AccountsServiceClient(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackTransfer")
    public ResponseEntity<MessageResponseDto> doTransfer(ExchangedTransferDtoWithToken exchangedTransferDto) {
        return restTemplate.postForEntity(
                "http://accounts-service/transfer",
                exchangedTransferDto,
                MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackTransfer(
            ExchangedTransferDtoWithToken exchangedTransferDto,
            Throwable ex
    ) {
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", "Переводы недоступны");
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }
}
