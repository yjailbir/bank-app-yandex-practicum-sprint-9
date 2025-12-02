package ru.yjailbir.transferservice.service;

import io.micrometer.tracing.Tracer;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.client.BlockerServiceClient;
import ru.yjailbir.commonslib.client.NotificationClient;
import ru.yjailbir.commonslib.dto.request.ExchangedTransferDtoWithToken;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.ExchangeResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.transferservice.client.AccountsServiceClient;
import ru.yjailbir.transferservice.client.ExchangeServiceClient;

@Service
public class TransferService {
    Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final AccountsServiceClient accountsServiceClient;
    private final ExchangeServiceClient exchangeServiceClient;
    private final NotificationClient notificationClient;
    private final BlockerServiceClient blockerServiceClient;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;


    @Autowired
    public TransferService(
            AccountsServiceClient accountsServiceClient,
            ExchangeServiceClient exchangeServiceClient,
            NotificationClient notificationClient,
            BlockerServiceClient blockerServiceClient,
            MeterRegistry meterRegistry,
            Tracer tracer
    ) {
        this.accountsServiceClient = accountsServiceClient;
        this.exchangeServiceClient = exchangeServiceClient;
        this.notificationClient = notificationClient;
        this.blockerServiceClient = blockerServiceClient;
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
    }

    public ResponseEntity<MessageResponseDto> doTransfer(TransferRequestDtoWithToken dto) {
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
                    meterRegistry.counter(
                            "transfer_errors_total",
                            "fromCurrency", dto.fromCurrency(),
                            "fromToken", dto.token(),
                            "toCurrency", dto.toCurrency(),
                            "toLogin", dto.toLogin()
                    ).increment();
                    logger.error(
                            "Transfer error: {}. TraceId: {}",
                            exchangeResponseDto.message, tracer.currentSpan().context().traceId()
                    );
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
                                    dto.token()
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
                meterRegistry.counter(
                        "transfer_errors_total",
                        "fromCurrency", dto.fromCurrency(),
                        "fromToken", dto.token(),
                        "toCurrency", dto.toCurrency(),
                        "toLogin", dto.toLogin()
                ).increment();
                logger.error(
                        "Transfer error: {}. TraceId: {}",
                        messageResponseDto.message(), tracer.currentSpan().context().traceId()
                );
                return ResponseEntity.badRequest().body(messageResponseDto);
            }

            notificationClient.sendNotification(
                    "В сервисе переводов совершён перевод на сумму " + dto.value() + " "
                            + dto.fromCurrency() + " пользователем с токеном " + dto.token()
            );

            return ResponseEntity.ok(messageResponseDto);
        }
    }
}
