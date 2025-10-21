package ru.yjailbir.commonslib.dto.request;

public record TransferRequestDtoWithToken(String fromCurrency, String toCurrency, Double value, String toLogin, String token) {
}
