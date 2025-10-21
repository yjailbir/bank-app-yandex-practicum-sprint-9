package ru.yjailbir.commonslib.dto.request;

public record ExchangedTransferDtoWithToken(
        String fromCurrency,
        String toCurrency,
        Double fromValue,
        Double toValue,
        String toLogin,
        String token
) {
}
