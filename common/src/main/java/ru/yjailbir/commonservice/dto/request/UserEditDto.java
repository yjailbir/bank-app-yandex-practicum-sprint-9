package ru.yjailbir.commonservice.dto.request;

import java.util.List;

public record UserEditDto(String name, String surname, List<String> activeAccounts) {
}
