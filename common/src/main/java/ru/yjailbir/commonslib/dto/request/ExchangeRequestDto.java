package ru.yjailbir.commonslib.dto.request;

public record ExchangeRequestDto(String currencyFrom, String currencyTo, Double valueFrom) {
}
