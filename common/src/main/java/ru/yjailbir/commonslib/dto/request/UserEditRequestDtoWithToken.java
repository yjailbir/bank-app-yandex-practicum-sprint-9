package ru.yjailbir.commonslib.dto.request;

import java.util.List;

public record UserEditRequestDtoWithToken(String name, String surname, List<String> activeAccounts, String token){
}
