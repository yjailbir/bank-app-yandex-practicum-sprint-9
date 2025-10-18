package ru.yjailbir.uiservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {
    private final RestTemplate restTemplate;

    @Autowired
    public CourseController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/rates")
    public ResponseEntity<List<CurrencyRateDto>> getRates() {
        ResponseEntity<List<CurrencyRateDto>> cashResponseEntity = restTemplate.exchange(
                "http://exchange-generator-service/course",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        return ResponseEntity.ok(cashResponseEntity.getBody());
    }
}
