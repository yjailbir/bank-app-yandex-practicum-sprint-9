package ru.yjailbir.uiservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.LoginRequestDto;
import ru.yjailbir.commonslib.dto.request.RegisterRequestDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

@Service
public class AuthService {
    private final RestTemplate restTemplate;

    @Autowired
    public AuthService(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<MessageResponseDto> register(RegisterRequestDto dto) {
        return restTemplate.postForEntity(
                "http://accounts-service/register", dto, MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> login(LoginRequestDto dto) {
        return restTemplate.postForEntity(
                "http://accounts-service/login", dto, MessageResponseDto.class
        );
    }
}
