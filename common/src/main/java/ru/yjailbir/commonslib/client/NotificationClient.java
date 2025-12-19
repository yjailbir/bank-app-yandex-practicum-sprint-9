package ru.yjailbir.commonslib.client;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.request.NotificationDto;

@Service
public class NotificationClient {
    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    @Autowired
    public NotificationClient(KafkaTemplate<String, NotificationDto> kafkaTemplate, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.meterRegistry = meterRegistry;
    }

    public void sendNotification(String message) {
        NotificationDto dto = new NotificationDto(message);

        kafkaTemplate.send("notification-topic", dto)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        meterRegistry.counter("notification-errors-total").increment();
                    }
                });
    }
}
