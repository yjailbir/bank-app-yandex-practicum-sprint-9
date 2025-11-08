package ru.yjailbir.uiservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;

import java.util.List;

@Service
public class CourseService {
    private final RestTemplate restTemplate;

    @Autowired
    public CourseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<List<CurrencyRateDto>> getRates() {
        return restTemplate.exchange(
                "http://exchange-service:8080/course",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
    }
}
