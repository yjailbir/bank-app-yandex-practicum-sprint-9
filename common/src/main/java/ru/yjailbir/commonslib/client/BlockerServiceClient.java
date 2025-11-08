package ru.yjailbir.commonslib.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.util.AuthorizedHttpEntityFactory;

@Service
public class BlockerServiceClient {
    private final RestTemplate restTemplate;
    private final String errorMessage = "Сервис проверки подозрительной активности недоступен";
    private final String url = "http://blocker-service:8080/";

    @Autowired
    public BlockerServiceClient(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "blockerService")
    @CircuitBreaker(name = "blockerService", fallbackMethod = "fallbackCheckCashOperation")
    public ResponseEntity<MessageResponseDto> checkCashOperation(CashRequestDtoWithToken dto) {
        HttpEntity<CashRequestDtoWithToken> blockerRequestEntity =
                new AuthorizedHttpEntityFactory<CashRequestDtoWithToken>()
                        .createHttpEntityWithToken(dto, dto.token());

        return restTemplate.postForEntity(
                this.url + "check-cash-operation",
                blockerRequestEntity,
                MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackCheckCashOperation(CashRequestDtoWithToken dto, Throwable ex) {
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", this.errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }

    @Retry(name = "blockerService")
    @CircuitBreaker(name = "blockerService", fallbackMethod = "fallbackCheckTransferOperation")
    public ResponseEntity<MessageResponseDto> checkTransferOperation(TransferRequestDtoWithToken dto) {
        HttpEntity<TransferRequestDtoWithToken> blockerRequestEntity =
                new AuthorizedHttpEntityFactory<TransferRequestDtoWithToken>()
                        .createHttpEntityWithToken(dto, dto.token());

        return restTemplate.postForEntity(
                this.url + "check-transfer-operation",
                blockerRequestEntity,
                MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackCheckTransferOperation(TransferRequestDtoWithToken dto, Throwable ex) {
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", this.errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }
}
