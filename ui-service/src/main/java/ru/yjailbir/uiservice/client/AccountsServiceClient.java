package ru.yjailbir.uiservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yjailbir.commonslib.dto.request.*;
import ru.yjailbir.commonslib.dto.response.AllUserLoginsResponseDto;
import ru.yjailbir.commonslib.dto.response.MessageResponseDto;
import ru.yjailbir.commonslib.dto.response.UserAccountsResponseDto;
import ru.yjailbir.commonslib.dto.response.UserDataResponseDto;

import java.util.List;

@Service
public class AccountsServiceClient {
    private final RestTemplate restTemplate;
    private final String errorMessage = "Сервис учётных записей недоступен";
    private final String url = "http://accounts-service:8080/";

    @Autowired
    public AccountsServiceClient(RestTemplate restTemplate) {
        restTemplate.setErrorHandler(response -> {
            // Чтобы не летели исключения на 4хх и 5хх коды. Обрабатываем коды вручную
            return false;
        });
        this.restTemplate = restTemplate;
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackUserData")
    public ResponseEntity<UserDataResponseDto> getUserData(String token) {
        return restTemplate.postForEntity(
                this.url + "user-data",
                new EmptyRequestDtoWithToken(token), UserDataResponseDto.class
        );
    }

    public ResponseEntity<UserDataResponseDto> fallbackUserData(String token, Throwable ex) {
        UserDataResponseDto dto = new UserDataResponseDto();
        dto.status = "error";
        dto.message = this.errorMessage;
        dto.login = null;
        dto.name = null;
        dto.surname = null;
        dto.accounts = List.of();
        return ResponseEntity.internalServerError().body(dto);
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackUserAccounts")
    public ResponseEntity<UserAccountsResponseDto> getUserAccounts(String token) {
        return restTemplate.postForEntity(
                this.url + "active-accounts",
                new EmptyRequestDtoWithToken(token), UserAccountsResponseDto.class
        );
    }

    public ResponseEntity<UserAccountsResponseDto> fallbackUserAccounts(String token, Throwable ex) {
        UserAccountsResponseDto dto = new UserAccountsResponseDto();
        dto.status = "error";
        dto.message = this.errorMessage;
        dto.accounts = List.of();
        return ResponseEntity.internalServerError().body(dto);
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackAllUserLogins")
    public ResponseEntity<AllUserLoginsResponseDto> getAllUserLogins(String token) {
        return restTemplate.postForEntity(
                this.url + "all-logins",
                new EmptyRequestDtoWithToken(token), AllUserLoginsResponseDto.class
        );
    }

    public ResponseEntity<AllUserLoginsResponseDto> fallbackAllUserLogins(String token, Throwable ex) {
        AllUserLoginsResponseDto dto = new AllUserLoginsResponseDto();
        dto.status = "error";
        dto.message = this.errorMessage;
        dto.logins = List.of();
        return ResponseEntity.internalServerError().body(dto);
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackChangePassword")
    public ResponseEntity<MessageResponseDto> changePassword(String token, PasswordChangeRequestDto dto) {
        return restTemplate.postForEntity(
                this.url + "change-password",
                new PasswordChangeRequestDtoWithToken(dto.password(), token), MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackChangePassword(
            String token,
            PasswordChangeRequestDto dto,
            Throwable ex
    ) {
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", this.errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }

    @Retry(name = "accountsService")
    @CircuitBreaker(name = "accountsService", fallbackMethod = "fallbackEditUser")
    public ResponseEntity<MessageResponseDto> editUser(String token, UserEditRequestDto dto) {
        System.out.println("Отправляю на адрес: " + this.url + "edit");
        return restTemplate.postForEntity(
                this.url + "edit",
                new UserEditRequestDtoWithToken(dto.name(), dto.surname(), dto.activeAccounts(), token),
                MessageResponseDto.class
        );
    }

    public ResponseEntity<MessageResponseDto> fallbackEditUser(String token, UserEditRequestDto dto, Throwable ex) {
        MessageResponseDto messageResponseDto = new MessageResponseDto("error", this.errorMessage);
        return ResponseEntity.internalServerError().body(messageResponseDto);
    }
}
