package ru.yjailbir.commonslib.dto.request;

import java.time.LocalDate;

public record RegisterRequestDto(String login, String password, String surname, String name, LocalDate birthDate) {
}
