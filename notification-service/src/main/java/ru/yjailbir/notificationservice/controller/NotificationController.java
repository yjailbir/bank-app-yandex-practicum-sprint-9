package ru.yjailbir.notificationservice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yjailbir.commonslib.dto.request.NotificationDto;

import java.util.logging.Logger;

@RestController
public class NotificationController {
   private final Logger logger = Logger.getLogger(NotificationController.class.getName());

    @PostMapping("/notify")
    public String doNotification(@RequestBody NotificationDto dto) {
        logger.info("Received notification: " + dto.text());
        return "notification sent";
    }
}
