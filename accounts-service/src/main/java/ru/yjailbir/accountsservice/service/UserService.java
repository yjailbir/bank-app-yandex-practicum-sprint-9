package ru.yjailbir.accountsservice.service;

import ru.yjailbir.accountsservice.entity.AccountEntity;
import ru.yjailbir.accountsservice.repository.AccountsRepository;
import ru.yjailbir.commonslib.dto.CurrencyDto;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;
import ru.yjailbir.commonslib.dto.AccountDto;
import ru.yjailbir.commonslib.dto.request.*;
import ru.yjailbir.commonslib.dto.response.UserAccountsResponseDto;
import ru.yjailbir.commonslib.dto.response.UserDataResponseDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountsRepository accountsRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, AccountsRepository accountsRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.accountsRepository = accountsRepository;
        this.jwtUtil = jwtUtil;
    }

    public void saveNewUser(RegisterRequestDto dto) {
        if (userRepository.findByLogin(dto.login()).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя занято!");
        }
        if (Period.between(dto.birthDate(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Возраст должен быть не меньше 18 лет!");
        }

        userRepository.save(new UserEntity(
                dto.login(),
                hashPassword(dto.password()),
                dto.surname(),
                dto.name(),
                dto.birthDate()
        ));

        UserEntity user = getUserEntityByLogin(dto.login());
        user.getAccounts().addAll(List.of(
                new AccountEntity("RUB", "Российский рубль", user),
                new AccountEntity("USD", "Американский доллар", user),
                new AccountEntity("SPM", "Имперский септим", user)
        ));
        userRepository.save(user);
    }

    public String loginUser(LoginRequestDto dto) {
        UserEntity user = getUserEntityByLogin(dto.login());

        if (!verifyPassword(dto.password(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный пароль!");
        }

        return jwtUtil.generateJwtToken(user);
    }

    public void updateUserPassword(PasswordChangeRequestDtoWithToken dto) {
        UserEntity user = getUserEntityByLogin(jwtUtil.getLoginFromJwtToken(dto.token()));
        user.setPassword(hashPassword(dto.password()));
        userRepository.save(user);
    }

    public UserDataResponseDto getUserData(String token) {
        UserEntity user = getUserEntityByLogin(jwtUtil.getLoginFromJwtToken(token));
        List<AccountDto> accounts = user.getAccounts().stream().map(
                accountEntity ->
                        new AccountDto(
                                accountEntity.getCurrency(),
                                accountEntity.getName(),
                                accountEntity.getBalance(),
                                accountEntity.isActive()
                        )
        ).toList();
        return new UserDataResponseDto("ok", user.getLogin(), user.getName(), user.getSurname(), accounts);
    }

    public void updateUser(UserEditRequestDtoWithToken dto) {
        UserEntity user = getUserEntityByLogin(jwtUtil.getLoginFromJwtToken(dto.token()));

        if (dto.name() != null && !dto.name().isEmpty() && !dto.name().isBlank()) {
            user.setName(dto.name());
        }
        if (dto.surname() != null && !dto.surname().isEmpty() && !dto.surname().isBlank()) {
            user.setSurname(dto.surname());
        }

        List<String> activeAccounts = (dto.activeAccounts() == null) ? new ArrayList<>() : dto.activeAccounts();
        user.getAccounts().forEach(
                accountEntity -> accountEntity.setActive(activeAccounts.contains(accountEntity.getCurrency()))
        );
        userRepository.save(user);
    }

    public UserAccountsResponseDto getUserActiveAccounts(String token) {
        UserEntity user = getUserEntityByLogin(jwtUtil.getLoginFromJwtToken(token));
        List<CurrencyDto> accounts = user.getAccounts().stream().filter(AccountEntity::isActive).map(
                x -> new CurrencyDto(x.getCurrency(), x.getName())
        ).toList();

        return new UserAccountsResponseDto("ok", accounts);
    }

    public void doCashOperation(CashRequestDtoWithToken dto) {
        UserEntity user = getUserEntityByLogin(jwtUtil.getLoginFromJwtToken(dto.token()));
        changeAccountBalance(
                accountsRepository.findByCurrencyAndUser_Id(dto.currency(), user.getId()),
                dto.value(),
                dto.action()
        );
    }

    private void changeAccountBalance(AccountEntity account, Integer value, String action) {
        if(value < 0) {
            throw new IllegalArgumentException("Число должно быть неотрицательным!");
        }

        switch (action) {
            case "PUT" -> account.setBalance(account.getBalance() + value);
            case "GET" -> {
                if(account.getBalance() < value) {
                    throw new IllegalArgumentException("Недостаточно средств!");
                } else {
                    account.setBalance(account.getBalance() - value);
                }
            }
            default -> throw new IllegalArgumentException("Неверное действие!");
        }

        accountsRepository.save(account);
    }

    public String validateToken(String token) {
        return jwtUtil.validateJwtToken(token);
    }

    public String getLoginFromToken(String token) {
        return jwtUtil.getLoginFromJwtToken(token);
    }

    private UserEntity getUserEntityByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не существует!"));
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
