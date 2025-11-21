package ru.yjailbir.exchangeservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.CurrencyRateDto;

import java.util.Arrays;

@Service
public class ExchangeKafkaConsumer {

    private final ExchangeService service;

    @Autowired
    public ExchangeKafkaConsumer(ExchangeService service) {
        this.service = service;
    }

    @KafkaListener(topics = "exchange-currency-topic", groupId = "exchange-service-group")
    public void listen(CurrencyRateDto[] rates) {
        service.updateRates(Arrays.asList(rates));
    }
}
