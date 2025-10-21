package ru.yjailbir.commonslib.dto.request;

public record TransferRequestDto(String fromCurrency, String toCurrency, Double value, String toLogin){
}
