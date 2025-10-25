package ru.yjailbir.blockerservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.blockerservice.service.BlockerService;
import ru.yjailbir.commonslib.dto.request.CashRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.request.TransferRequestDtoWithToken;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;

@RestController
public class BlockerController {
    private final BlockerService blockerService;

    @Autowired
    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }

    @PostMapping("/check-cash-operation")
    public ResponseEntity<MessageResponseDto> checkCashOperation(@RequestBody CashRequestDtoWithToken dto){
        return blockerService.checkOperation();
    }

    @PostMapping("/check-transfer-operation")
    public ResponseEntity<MessageResponseDto> checkTransferOperation(@RequestBody TransferRequestDtoWithToken dto){
        return blockerService.checkOperation();
    }
}
