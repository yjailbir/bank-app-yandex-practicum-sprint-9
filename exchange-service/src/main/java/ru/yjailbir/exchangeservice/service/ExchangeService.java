package ru.yjailbir.exchangeservice.service;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;
import ru.yjailbir.commonslib.dto.request.ExchangeRequestDto;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeService {
    private final Logger logger = LoggerFactory.getLogger(ExchangeService.class);

    private List<CurrencyRateDto> rates = new ArrayList<>();
    private final Tracer tracer;

    @Autowired
    public ExchangeService(Tracer tracer) {
        this.tracer = tracer;
    }

    public void updateRates(List<CurrencyRateDto> rates) {
        this.rates = rates;
    }

    public ExchangeResponseDto doExchange(ExchangeRequestDto dto) {
        double result;
        if (dto.currencyFrom().equals("RUB")) {
            result = dto.valueFrom() / getRate(rates, dto.currencyTo());
        } else if (dto.currencyTo().equals("RUB")) {
            result = dto.valueFrom() * getRate(rates, dto.currencyFrom());
        } else {
            result = dto.valueFrom() * getRate(rates, dto.currencyFrom()) / getRate(rates, dto.currencyTo());
        }

        return new ExchangeResponseDto("ok", result);
    }

    private double getRate(List<CurrencyRateDto> rates, String currency) {
        return rates.stream()
                .filter(x -> x.name().equals(currency))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Currency {} not found. TraceId: {}", currency, tracer.currentSpan().context().traceId());
                    return new IllegalArgumentException("Unknown currency: " + currency);
                })
                .value();
    }

    public List<CurrencyRateDto> getRates() {
        return rates;
    }
}
