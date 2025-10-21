package ru.yjailbir.commonslib.dto.response;

public class ExchangeResponseDto {
    public String status;
    public String message;
    public Double convertedValue;

    public ExchangeResponseDto() {
    }

    public ExchangeResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ExchangeResponseDto(String status, Double convertedValue) {
        this.status = status;
        this.convertedValue = convertedValue;
    }
}
