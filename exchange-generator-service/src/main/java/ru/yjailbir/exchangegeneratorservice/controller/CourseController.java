package ru.yjailbir.exchangegeneratorservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;
import ru.yjailbir.exchangegeneratorservice.dto.ExchangeCourse;
import ru.yjailbir.exchangegeneratorservice.service.Generator;

import java.util.List;

@RestController
public class CourseController {
    private final Generator generator;

    @Autowired
    public CourseController(Generator generator) {
        this.generator = generator;
    }

    @GetMapping("/course")
    public ResponseEntity<List<CurrencyRateDto>> getCourse() {
        ExchangeCourse course = generator.getCourse();
        List<CurrencyRateDto> list = List.of(
                new CurrencyRateDto(
                        "Американский доллар", "USD", Math.round(course.rubForUsd() * 100.0) / 100.0
                ),
                new CurrencyRateDto(
                        "Имперский септим", "ISM",
                        Math.round(course.rubForIsm() * 100.0) / 100.0
                )
        );

        return ResponseEntity.ok(list);
    }
}
