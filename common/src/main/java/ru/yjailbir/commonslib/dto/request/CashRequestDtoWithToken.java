package ru.yjailbir.commonslib.dto.request;

public record CashRequestDtoWithToken(String currency, Double value, String action, String token) {
}
