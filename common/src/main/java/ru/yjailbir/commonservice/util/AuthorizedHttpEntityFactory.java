package ru.yjailbir.commonservice.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class AuthorizedHttpEntityFactory<T> {
    public HttpEntity<T> createHttpEntityWithToken(T dto, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(dto, headers);
    }
}
