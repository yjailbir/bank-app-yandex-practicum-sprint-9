package ru.yjailbir.exchangegeneratorservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yjailbir.exchangegeneratorservice.dto.ExchangeCourse;


import java.util.Random;

@Component
public class Generator {
    private final Random random;
    private volatile ExchangeCourse dto;

    public Generator() {
        random = new Random();
    }

    @Scheduled(fixedRate = 1000)
    public void updateCourse() {
        dto = new ExchangeCourse(
                50 + (120 - 50) * random.nextDouble(),
                100 + (300 - 100) * random.nextDouble()
        );
    }

    public ExchangeCourse getCourse() {
        return dto;
    }
}
