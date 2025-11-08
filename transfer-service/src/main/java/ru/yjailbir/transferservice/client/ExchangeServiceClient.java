package ru.yjailbir.transferservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.ExchangeRequestDto;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;

@Service
public class ExchangeServiceClient {
    private final RestTemplate restTemplate;

    @Autowired
    public ExchangeServiceClient( RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "exchangeService")
    @CircuitBreaker(name = "exchangeService", fallbackMethod = "fallbackExchange")
    public ResponseEntity<ExchangeResponseDto> doExchange(TransferRequestDtoWithToken dto) {
        return  restTemplate.postForEntity(
                "http://exchange-service:8080/exchange",
                new ExchangeRequestDto(dto.fromCurrency(), dto.toCurrency(), dto.value()),
                ExchangeResponseDto.class
        );
    }

    public ResponseEntity<ExchangeResponseDto> fallbackExchange(TransferRequestDtoWithToken dto, Throwable ex) {
        ExchangeResponseDto errorDto = new ExchangeResponseDto();
        errorDto.status = "error";
        errorDto.message = "Сервис конвертации недоступен";
        errorDto.convertedValue = null;
        return ResponseEntity.internalServerError().body(errorDto);
    }
}
