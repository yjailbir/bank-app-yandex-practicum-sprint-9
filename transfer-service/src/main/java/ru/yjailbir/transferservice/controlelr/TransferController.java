package ru.yjailbir.transferservice.controlelr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.client.BlockerServiceClient;
import ru.yjailbir.commonslib.client.NotificationClient;
import ru.yjailbir.commonslib.dto.request.ExchangedTransferDtoWithToken;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.transferservice.client.AccountsServiceClient;
import ru.yjailbir.transferservice.client.ExchangeServiceClient;

@RestController
public class TransferController {
    private final AccountsServiceClient accountsServiceClient;
    private final ExchangeServiceClient exchangeServiceClient;
    private final NotificationClient notificationClient;
    private final BlockerServiceClient blockerServiceClient;

    @Autowired
    public TransferController(
            AccountsServiceClient accountsServiceClient,
            ExchangeServiceClient exchangeServiceClient,
            NotificationClient notificationClient,
            BlockerServiceClient blockerServiceClient
    ) {
        this.accountsServiceClient = accountsServiceClient;
        this.exchangeServiceClient = exchangeServiceClient;
        this.notificationClient = notificationClient;
        this.blockerServiceClient = blockerServiceClient;
    }

    @PostMapping("/transfer")
    public ResponseEntity<MessageResponseDto> doTransfer(@RequestBody TransferRequestDtoWithToken dto) {
        ResponseEntity<MessageResponseDto> blockerResponseEntity = blockerServiceClient.checkTransferOperation(dto);
        MessageResponseDto blockerResponseDto = blockerResponseEntity.getBody();

        assert blockerResponseDto != null;
        if (!blockerResponseEntity.getStatusCode().is2xxSuccessful() || !blockerResponseDto.status().equals("ok")) {
            return ResponseEntity.badRequest().body(blockerResponseDto);
        } else {
            ExchangedTransferDtoWithToken exchangedTransferDto;
            if (!dto.toCurrency().equals(dto.fromCurrency())) {
                ResponseEntity<ExchangeResponseDto> exchangeResponseEntity = exchangeServiceClient.doExchange(dto);
                ExchangeResponseDto exchangeResponseDto = exchangeResponseEntity.getBody();

                assert exchangeResponseDto != null;
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
                exchangedTransferDto = new ExchangedTransferDtoWithToken(
                        dto.fromCurrency(),
                        dto.toCurrency(),
                        dto.value(),
                        dto.value(),
                        dto.toLogin(),
                        dto.token()
                );
            }

            ResponseEntity<MessageResponseDto> responseEntity = accountsServiceClient.doTransfer(exchangedTransferDto);
            MessageResponseDto messageResponseDto = responseEntity.getBody();

            assert messageResponseDto != null;
            if (!responseEntity.getStatusCode().is2xxSuccessful() || !messageResponseDto.status().equals("ok")) {
                return ResponseEntity.badRequest().body(messageResponseDto);
            }

            notificationClient.sendNotification(
                    "В сервисе переводов совершён перевод на сумму " + dto.value() + " "
                            + dto.fromCurrency() + " пользователем с токеном " + dto.token(), dto.token()
            );

            return ResponseEntity.ok(messageResponseDto);
        }
    }
}
