package ru.yjailbir.exchangeservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;
import ru.yjailbir.commonslib.dto.request.ExchangeRequestDto;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;
import ru.yjailbir.exchangeservice.service.ExchangeService;

import java.util.List;

@RestController
public class ExchangeController {
    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/exchange")
    public ResponseEntity<ExchangeResponseDto> exchange(@RequestBody ExchangeRequestDto dto) {
        return ResponseEntity.ok(exchangeService.doExchange(dto));
    }

    @PostMapping("/update-course")
    public void updateCourse(@RequestBody List<CurrencyRateDto> dto) {
        exchangeService.updateRates(dto);
    }

    @GetMapping("/course")
    public ResponseEntity<List<CurrencyRateDto>> getCourse() {
        return ResponseEntity.ok(exchangeService.getRates());
    }
}
