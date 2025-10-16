package ru.yjailbir.commonslib.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(name = "jwt.validation.enabled", havingValue = "true", matchIfMissing = true)
public class JwtSecurityConfiguration {
    @Bean
    public JwtValidationFilter jwtValidationFilterBean(RestTemplate restTemplate) {
        return new JwtValidationFilter(restTemplate);
    }

    @Bean
    public FilterRegistrationBean<JwtValidationFilter> jwtValidationFilter(JwtValidationFilter filter) {
        FilterRegistrationBean<JwtValidationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
