package ru.yjailbir.commonslib.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.NotificationDto;
import ru.yjailbir.commonslib.util.AuthorizedHttpEntityFactory;

@Service
public class NotificationClient {
    private final RestTemplate restTemplate;

    @Autowired
    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendNotification(String message, String token) {
        NotificationDto notificationDto = new NotificationDto(message);
        HttpEntity<NotificationDto> notificationRequestEntity =
                new AuthorizedHttpEntityFactory<NotificationDto>().
                        createHttpEntityWithToken(notificationDto, token);
        restTemplate.postForObject(
                "http://notification-service:8080/notify", notificationRequestEntity, String.class
        );
    }
}
