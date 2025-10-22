package ru.yjailbir.exchangeservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeGeneratorClient {
    private final RestTemplate restTemplate;

    @Autowired
    public ExchangeGeneratorClient( RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "exchangeGenerator")
    @CircuitBreaker(name = "exchangeGenerator", fallbackMethod = "fallbackExchangeRate")
    public ResponseEntity<List<CurrencyRateDto>> getExchangeRate() {
        return restTemplate.exchange(
                "http://exchange-generator-service/course",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public ResponseEntity<List<CurrencyRateDto>> fallbackExchangeRate(Throwable ex) {
        return ResponseEntity.internalServerError().body(new ArrayList<>());
    }
}
