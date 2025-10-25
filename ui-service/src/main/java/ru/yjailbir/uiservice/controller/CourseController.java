package ru.yjailbir.uiservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;
import ru.yjailbir.uiservice.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseController {
    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/rates")
    public ResponseEntity<List<CurrencyRateDto>> getRates() {
        ResponseEntity<List<CurrencyRateDto>> cashResponseEntity = courseService.getRates();

        return ResponseEntity.ok(cashResponseEntity.getBody());
    }
}
