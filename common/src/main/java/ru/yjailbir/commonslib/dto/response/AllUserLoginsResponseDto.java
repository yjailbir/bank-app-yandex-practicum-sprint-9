package ru.yjailbir.commonslib.dto.response;

import java.util.List;

public class AllUserLoginsResponseDto {
    public String status;
    public String message;
    public List<String> logins;

    public AllUserLoginsResponseDto() {
    }

    public AllUserLoginsResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public AllUserLoginsResponseDto(String status, List<String> logins) {
        this.status = status;
        this.logins = logins;
    }
}
