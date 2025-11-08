package ru.yjailbir.uiservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.CashRequestDto;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.util.AuthorizedHttpEntityFactory;

@Service
public class CashServiceClient {
    private final RestTemplate restTemplate;

    @Autowired
    public CashServiceClient(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "cashService")
    @CircuitBreaker(name = "cashService", fallbackMethod = "fallbackCash")
    public ResponseEntity<MessageResponseDto> doCash(String token, CashRequestDto dto) {
        HttpEntity<CashRequestDtoWithToken> cashRequestEntity =
                new AuthorizedHttpEntityFactory<CashRequestDtoWithToken>()
                        .createHttpEntityWithToken(new CashRequestDtoWithToken(
                                dto.currency(), dto.value(), dto.action(), token), token
                        );

        return restTemplate.postForEntity(
                "http://cash-service:8080/operate", cashRequestEntity, MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackCash(String token, CashRequestDto dto, Throwable ex) {
        String errorMessage = "Сервис наличных недоступен";
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }
}
