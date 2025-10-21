package ru.yjailbir.cashservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.client.BlockerClient;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.client.NotificationClient;

@RestController
public class CashController {
    private final RestTemplate restTemplate;
    private final NotificationClient notificationClient;
    private final BlockerClient blockerClient;

    @Autowired
    public CashController(RestTemplate restTemplate, NotificationClient notificationClient, BlockerClient blockerClient) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
        this.notificationClient = notificationClient;
        this.blockerClient = blockerClient;
    }

    @PostMapping("/operate")
    public ResponseEntity<MessageResponseDto> cashOperation(@RequestBody CashRequestDtoWithToken dto) {
        ResponseEntity<MessageResponseDto> blockerResponseEntity = blockerClient.checkCashOperation(dto);
        MessageResponseDto blockerResponseDto = blockerResponseEntity.getBody();

        if (blockerResponseDto != null) {
            if (!blockerResponseEntity.getStatusCode().is2xxSuccessful() || !blockerResponseDto.status().equals("ok")) {
                return ResponseEntity.badRequest().body(blockerResponseDto);
            } else {
                ResponseEntity<MessageResponseDto> responseEntity = restTemplate.postForEntity(
                        "http://accounts-service/cash",
                        dto,
                        MessageResponseDto.class
                );
                MessageResponseDto messageResponseDto = responseEntity.getBody();

                if (messageResponseDto != null) {
                    if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                        return ResponseEntity.badRequest().body(messageResponseDto);
                    }
                } else {
                    return ResponseEntity.badRequest().body(new MessageResponseDto(
                            "error", "Операции с наличными недоступны"
                    ));
                }

                notificationClient.sendNotification(
                        "В сервисе наличных произведена операция " + dto.action() + " на сумму " + dto.value() +
                                " " + dto.currency() + " пользователем с токеном " + dto.token(), dto.token()
                );
                
                return ResponseEntity.ok(messageResponseDto);
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponseDto(
                    "error", "Сервис проверки подозрительной активности недоступен"
            ));
        }
    }
}
