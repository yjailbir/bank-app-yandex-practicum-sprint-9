package ru.yjailbir.commonservice.dto.response;

public class UserDataResponseDto {
    public String status;
    public String message;
    public String login;
    public String name;
    public String surname;

    public UserDataResponseDto() {
    }

    public UserDataResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public UserDataResponseDto(String status,String login, String name, String surname) {
        this.status = status;
        this.login = login;
        this.name = name;
        this.surname = surname;
    }
}
