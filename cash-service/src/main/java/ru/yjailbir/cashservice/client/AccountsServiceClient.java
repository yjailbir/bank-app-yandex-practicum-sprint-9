package ru.yjailbir.cashservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
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
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackCashOperation")
    public ResponseEntity<MessageResponseDto> doCashOperation(CashRequestDtoWithToken dto){
        String url = "http://accounts-service:8080/";
        return restTemplate.postForEntity(
                url + "cash",
                dto,
                MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackCashOperation(CashRequestDtoWithToken dto, Throwable ex){
        String errorMessage = "Сервис учётных записей недоступен";
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }
}
