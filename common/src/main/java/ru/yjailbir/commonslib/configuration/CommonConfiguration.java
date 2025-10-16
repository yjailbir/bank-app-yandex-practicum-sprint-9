package ru.yjailbir.commonslib.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.client.BlockerClient;
import ru.yjailbir.commonslib.client.NotificationClient;

@Configuration
public class CommonConfiguration {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public NotificationClient notificator(){
        return new NotificationClient(restTemplate());
    }

    @Bean
    public BlockerClient blockerClient(){
        return new BlockerClient(restTemplate());
    }
}
