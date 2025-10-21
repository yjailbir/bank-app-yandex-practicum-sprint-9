package ru.yjailbir.transferservice.controlelr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.client.BlockerClient;
import ru.yjailbir.commonslib.client.NotificationClient;
import ru.yjailbir.commonslib.dto.request.ExchangeRequestDto;
import ru.yjailbir.commonslib.dto.request.ExchangedTransferDtoWithToken;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

@RestController
public class TransferController {
    private final RestTemplate restTemplate;
    private final NotificationClient notificationClient;
    private final BlockerClient blockerClient;

    @Autowired
    public TransferController(RestTemplate restTemplate, NotificationClient notificationClient, BlockerClient blockerClient) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
        this.notificationClient = notificationClient;
        this.blockerClient = blockerClient;
    }

    @PostMapping("/transfer")
    public ResponseEntity<MessageResponseDto> doTransfer(@RequestBody TransferRequestDtoWithToken dto) {
        ResponseEntity<MessageResponseDto> blockerResponseEntity = blockerClient.checkTransferOperation(dto);
        MessageResponseDto blockerResponseDto = blockerResponseEntity.getBody();

        if (blockerResponseDto != null) {
            if (!blockerResponseEntity.getStatusCode().is2xxSuccessful() || !blockerResponseDto.status().equals("ok")) {
                return ResponseEntity.badRequest().body(blockerResponseDto);
            } else {
                ExchangedTransferDtoWithToken exchangedTransferDto;
                if (!dto.toCurrency().equals(dto.fromCurrency())) {
                    ResponseEntity<ExchangeResponseDto> exchangeResponseEntity = restTemplate.postForEntity(
                            "http://exchange-service/exchange",
                            new ExchangeRequestDto(dto.fromCurrency(), dto.toCurrency(), dto.value()),
                            ExchangeResponseDto.class
                    );
                    ExchangeResponseDto exchangeResponseDto = exchangeResponseEntity.getBody();

                    if (exchangeResponseDto != null) {
                        if (
                                !exchangeResponseEntity.getStatusCode().is2xxSuccessful() || !exchangeResponseDto.status.equals("ok")
                        ) {
                            return ResponseEntity.badRequest().body(new MessageResponseDto("error", exchangeResponseDto.message));
                        } else {
                            exchangedTransferDto = new ExchangedTransferDtoWithToken(
                                    dto.fromCurrency(),
                                    dto.toCurrency(),
                                    dto.value(),
                                    exchangeResponseDto.convertedValue,
                                    dto.toLogin(),
                                    dto.token()
                            );

                            notificationClient.sendNotification(
                                    "Осуществлена конвертация " + exchangedTransferDto.fromCurrency() + " " +
                                            exchangedTransferDto.fromValue() + " в " + exchangedTransferDto.toCurrency()
                                            + " " + exchangedTransferDto.toValue() + " пользователем с токеном " +
                                            dto.token(), dto.token()
                            );
                        }
                    } else {
                        return ResponseEntity.badRequest().body(
                                new MessageResponseDto("error", "Сервис конвертации недоступен")
                        );
                    }
                } else {
                    exchangedTransferDto = new ExchangedTransferDtoWithToken(
                            dto.fromCurrency(),
                            dto.toCurrency(),
                            dto.value(),
                            dto.value(),
                            dto.toLogin(),
                            dto.token()
                    );
                }

                ResponseEntity<MessageResponseDto> responseEntity = restTemplate.postForEntity(
                        "http://accounts-service/transfer",
                        exchangedTransferDto,
                        MessageResponseDto.class
                );
                MessageResponseDto messageResponseDto = responseEntity.getBody();

                if (messageResponseDto != null) {
                    if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                        return ResponseEntity.badRequest().body(messageResponseDto);
                    }
                } else {
                    return ResponseEntity.badRequest().body(new MessageResponseDto(
                            "error", "Переводы недоступны"
                    ));
                }

                notificationClient.sendNotification(
                        "В сервисе переводов совершён перевод на сумму " + dto.value() + " "
                                + dto.fromCurrency() + " пользователем с токеном " + dto.token(), dto.token()
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
