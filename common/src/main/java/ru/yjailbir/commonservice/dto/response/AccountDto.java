package ru.yjailbir.commonservice.dto.response;

public record AccountDto(String currency, String name, Integer balance, boolean active) {
}
