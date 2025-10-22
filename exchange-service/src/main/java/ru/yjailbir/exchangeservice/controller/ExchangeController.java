package ru.yjailbir.exchangeservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;
import ru.yjailbir.commonslib.dto.request.ExchangeRequestDto;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;
import ru.yjailbir.exchangeservice.client.ExchangeGeneratorClient;

import java.util.List;

@RestController
public class ExchangeController {
    private final ExchangeGeneratorClient exchangeGeneratorClient;

    @Autowired
    public ExchangeController(ExchangeGeneratorClient exchangeGeneratorClient) {
        this.exchangeGeneratorClient = exchangeGeneratorClient;
    }

    @PostMapping("/exchange")
    public ResponseEntity<ExchangeResponseDto> exchange(@RequestBody ExchangeRequestDto dto) {
        ResponseEntity<List<CurrencyRateDto>> cashResponseEntity = exchangeGeneratorClient.getExchangeRate();
        if(!cashResponseEntity.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.badRequest().body(new ExchangeResponseDto("error", "Курсы недоступны"));
        } else {
            List<CurrencyRateDto> rates = cashResponseEntity.getBody();
            assert rates != null;
            try {
                double result;
                if (dto.currencyFrom().equals("RUB")) {
                    result = dto.valueFrom() / getRate(rates, dto.currencyTo());
                } else if (dto.currencyTo().equals("RUB")) {
                    result = dto.valueFrom() * getRate(rates, dto.currencyFrom());
                } else {
                    result = dto.valueFrom() * getRate(rates, dto.currencyFrom()) / getRate(rates, dto.currencyTo());
                }
                return ResponseEntity.ok(new ExchangeResponseDto("ok", result));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new ExchangeResponseDto("error", e.getMessage()));
            }
        }
    }

    private double getRate(List<CurrencyRateDto> rates, String currency) {
        return rates.stream()
                .filter(x -> x.name().equals(currency))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + currency))
                .value();
    }
}
