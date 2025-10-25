package ru.yjailbir.transferservice.controlelr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.transferservice.service.TransferService;

@RestController
public class TransferController {
    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<MessageResponseDto> doTransfer(@RequestBody TransferRequestDtoWithToken dto) {
        return transferService.doTransfer(dto);
    }
}
