package ru.yjailbir.uiservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.client.NotificationClient;
import ru.yjailbir.commonslib.dto.request.TransferRequestDto;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.util.AuthorizedHttpEntityFactory;

@Service
public class TransferServiceClient {
    private final NotificationClient notificationClient;
    private final RestTemplate restTemplate;

    @Autowired
    public TransferServiceClient(NotificationClient notificationClient, RestTemplate restTemplate) {
        this.notificationClient = notificationClient;
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "transferService")
    @CircuitBreaker(name = "transferService", fallbackMethod = "fallbackTransfer")
    public ResponseEntity<MessageResponseDto> doTransfer(String token, TransferRequestDto dto) {
        HttpEntity<TransferRequestDtoWithToken> transferRequestEntity =
                new AuthorizedHttpEntityFactory<TransferRequestDtoWithToken>()
                        .createHttpEntityWithToken(
                                new TransferRequestDtoWithToken(
                                        dto.fromCurrency(), dto.toCurrency(), dto.value(), dto.toLogin(), token
                                ), token
                        );

        return restTemplate.postForEntity(
                "http://transfer-service:8080/transfer", transferRequestEntity, MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackTransfer(String token, TransferRequestDto dto, Throwable ex) {
        notificationClient.sendNotification(
                "Вызван fallback метод fallbackTransfer: " + ex.getMessage()
        );
        String errorMessage = "Сервис переводов недоступен";
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }
}
