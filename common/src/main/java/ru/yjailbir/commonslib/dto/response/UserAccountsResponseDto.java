package ru.yjailbir.commonslib.dto.response;

import ru.yjailbir.commonslib.dto.CurrencyDto;

import java.util.List;

public class UserAccountsResponseDto {
    public String status;
    public String message;
    public List<CurrencyDto> accounts;

    public UserAccountsResponseDto() {
    }

    public UserAccountsResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public UserAccountsResponseDto(String status, List<CurrencyDto> accounts) {
        this.status = status;
        this.accounts = accounts;
    }
}
