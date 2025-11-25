package ru.yjailbir.commonslib.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.request.NotificationDto;

@Service
public class NotificationClient {
    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Autowired
    public NotificationClient(KafkaTemplate<String, NotificationDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String message) {
        NotificationDto dto = new NotificationDto(message);
        kafkaTemplate.send("notification-topic", dto);
    }
}
