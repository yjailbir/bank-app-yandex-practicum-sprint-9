package ru.yjailbir.commonservice.dto.request;

import java.util.List;

public record UserEditRequestDtoWithToken(String name, String surname, List<String> activeAccounts, String token){
}
