package ru.yjailbir.commonslib.dto.request;

import java.util.List;

public record UserEditRequestDto(String name, String surname, List<String> activeAccounts) {
}
