package ru.yjailbir.commonservice.dto.request;

public record CashRequestDtoWithToken(String currency, Integer value, String action, String token) {
}
