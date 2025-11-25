package ru.yjailbir.notificationservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.yjailbir.commonslib.dto.request.NotificationDto;

import java.util.logging.Logger;

@Service
public class NotificationConsumer {
    private final Logger logger = Logger.getLogger(NotificationConsumer.class.getName());

    @KafkaListener(topics = "notification-topic", groupId = "notification-service-group")
    public void consume(NotificationDto dto, Acknowledgment ack) {
        try {
            logger.info("Received notification: " + dto.text());
            ack.acknowledge();
        } catch (Exception e) {
            logger.warning("Failed to process message: " + e.getMessage());
        }
    }
}
