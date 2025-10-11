package ru.yjailbir.commonservice.dto.response;

import java.util.List;

public class UserDataResponseDto {
    public String status;
    public String message;
    public String login;
    public String name;
    public String surname;
    public List<AccountDto> accounts;

    public UserDataResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public UserDataResponseDto(String status,String login, String name, String surname, List<AccountDto> accounts) {
        this.status = status;
        this.login = login;
        this.name = name;
        this.surname = surname;
        this.accounts = accounts;
    }
}
