package ru.yjailbir.exchangegeneratorservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;
import ru.yjailbir.exchangegeneratorservice.dto.ExchangeCourse;


import java.util.List;
import java.util.Random;

@Component
public class Generator {
    private final RestTemplate restTemplate;
    private final Random random;

    @Autowired
    public Generator( RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
        this.random = new Random();
    }

    private ExchangeCourse getCourse() {
        return new ExchangeCourse(
                50 + (120 - 50) * random.nextDouble(),
                100 + (300 - 100) * random.nextDouble()
        );
    }

    @Scheduled(fixedRate = 1000)
    private void sendData() {
        ExchangeCourse course = getCourse();

        List<CurrencyRateDto> list = List.of(
                new CurrencyRateDto(
                        "Американский доллар", "USD", Math.round(course.rubForUsd() * 100.0) / 100.0
                ),
                new CurrencyRateDto(
                        "Имперский септим", "ISP",
                        Math.round(course.rubForIsm() * 100.0) / 100.0
                )
        );

        restTemplate.postForLocation("http://exchange-service/update-course", list);
    }
}
