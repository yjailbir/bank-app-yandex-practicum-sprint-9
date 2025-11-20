package ru.yjailbir.commonslib.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.client.BlockerServiceClient;

@Configuration
public class CommonConfiguration {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BlockerServiceClient blockerClient(){
        return new BlockerServiceClient(restTemplate());
    }
}
