package ru.yjailbir.commonservice.dto.request;

import java.util.List;

public record UserEditRequestDto(String name, String surname, List<String> activeAccounts) {
}
