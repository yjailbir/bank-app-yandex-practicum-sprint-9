package ru.yjailbir.cashservice.controller;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.cashservice.client.AccountsServiceClient;
import ru.yjailbir.commonslib.client.BlockerServiceClient;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.client.NotificationClient;

@RestController
public class CashController {
    private final Logger logger = LoggerFactory.getLogger(CashController.class);

    private final AccountsServiceClient accountsServiceClient;
    private final NotificationClient notificationClient;
    private final BlockerServiceClient blockerServiceClient;
    private final Tracer tracer;

    @Autowired
    public CashController(
            AccountsServiceClient accountsServiceClient,
            NotificationClient notificationClient,
            BlockerServiceClient blockerServiceClient,
            Tracer tracer
    ) {
        this.accountsServiceClient = accountsServiceClient;
        this.notificationClient = notificationClient;
        this.blockerServiceClient = blockerServiceClient;
        this.tracer = tracer;
    }

    @PostMapping("/operate")
    public ResponseEntity<MessageResponseDto> cashOperation(@RequestBody CashRequestDtoWithToken dto) {
        ResponseEntity<MessageResponseDto> blockerResponseEntity = blockerServiceClient.checkCashOperation(dto);
        MessageResponseDto blockerResponseDto = blockerResponseEntity.getBody();

        assert blockerResponseDto != null;
        if (!blockerResponseEntity.getStatusCode().is2xxSuccessful() || !blockerResponseDto.status().equals("ok")) {
            return ResponseEntity.badRequest().body(blockerResponseDto);
        } else {
            ResponseEntity<MessageResponseDto> responseEntity = accountsServiceClient.doCashOperation(dto);
            MessageResponseDto messageResponseDto = responseEntity.getBody();

            assert messageResponseDto != null;
            if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                return ResponseEntity.badRequest().body(messageResponseDto);
            }

            String message = "В сервисе наличных произведена операция " + dto.action() + " на сумму " + dto.value() +
                    " " + dto.currency() + " пользователем с токеном " + dto.token();
            notificationClient.sendNotification(message);
            logger.info("{}. TraceId: {}", message, tracer.currentSpan().context().traceId());
            return ResponseEntity.ok(messageResponseDto);
        }
    }
}
