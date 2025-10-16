package ru.yjailbir.commonslib.dto.request;

public record CashRequestDtoWithToken(String currency, Integer value, String action, String token) {
}
