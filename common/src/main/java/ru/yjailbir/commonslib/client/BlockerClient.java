package ru.yjailbir.commonslib.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.util.AuthorizedHttpEntityFactory;

@Service
public class BlockerClient {
    RestTemplate restTemplate;

    @Autowired
    public BlockerClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<MessageResponseDto> checkCashOperation(CashRequestDtoWithToken dto) {
        HttpEntity<CashRequestDtoWithToken> blockerRequestEntity =
                new AuthorizedHttpEntityFactory<CashRequestDtoWithToken>()
                        .createHttpEntityWithToken(dto, dto.token());

        return restTemplate.postForEntity(
                "http://blocker-service/check-cash-operation",
                blockerRequestEntity,
                MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> checkTransferOperation(TransferRequestDtoWithToken dto) {
        HttpEntity<TransferRequestDtoWithToken> blockerRequestEntity =
                new AuthorizedHttpEntityFactory<TransferRequestDtoWithToken>()
                        .createHttpEntityWithToken(dto, dto.token());

        return restTemplate.postForEntity(
                "http://blocker-service/check-transfer-operation",
                blockerRequestEntity,
                MessageResponseDto.class
        );
    }
}
