package ru.yjailbir.accountsservice.service;

import ru.yjailbir.accountsservice.entity.AccountEntity;
import ru.yjailbir.commonservice.dto.request.*;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;
import ru.yjailbir.commonservice.dto.response.AccountDto;
import ru.yjailbir.commonservice.dto.response.UserDataResponseDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public void saveNewUser(RegisterRequestDto dto) {
        if (userRepository.findByLogin(dto.login()).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя занято!");
        }
        if(Period.between(dto.birthDate(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Возраст должен быть не меньше 18 лет!");
        }

        userRepository.save(new UserEntity(
                dto.login(),
                hashPassword(dto.password()),
                dto.surname(),
                dto.name(),
                dto.birthDate()
        ));

        UserEntity user = userRepository.findByLogin(dto.login()).get();
        user.getAccounts().addAll(List.of(
                new AccountEntity("RUB", "Российский рубль", user),
                new AccountEntity("USD", "Американский доллар", user),
                new AccountEntity("TMS", "Имперский септим", user)
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

    public void updateUserPassword(PasswordChangeDtoWithToken dto) {
        Optional<UserEntity> userOptional = userRepository.findByLogin(jwtUtil.getLoginFromJwtToken(dto.token()));
        if (userOptional.isPresent()) {
            UserEntity userEntity = userOptional.get();
            userEntity.setPassword(hashPassword(dto.password()));
            userRepository.save(userEntity);
        } else {
            //По идее это никогда не выбросится, потому что токен нельзя изменить, потому что он хранится на сервере
            throw new IllegalArgumentException("Пользователь не существует!");
        }
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

    public void updateUser(UserEditDtoWithToken dto) {
        Optional<UserEntity> userOptional = userRepository.findByLogin(jwtUtil.getLoginFromJwtToken(dto.token()));
        if (userOptional.isPresent()) {
            UserEntity userEntity = userOptional.get();
            if(dto.name() != null && !dto.name().isEmpty() && !dto.name().isBlank()) {
                userEntity.setName(dto.name());
            }
            if(dto.surname() != null && !dto.surname().isEmpty() && !dto.surname().isBlank()) {
                userEntity.setSurname(dto.surname());
            }
            List<String> activeAccounts = (dto.activeAccounts() == null) ? new ArrayList<>() : dto.activeAccounts();
            userEntity.getAccounts().forEach(
                    accountEntity -> accountEntity.setActive(activeAccounts.contains(accountEntity.getCurrency()))
            );
            userRepository.save(userEntity);
        } else {
            //По идее это никогда не выбросится, потому что токен нельзя изменить, потому что он хранится на сервере
            throw new IllegalArgumentException("Пользователь не существует!");
        }
    }

    public String validateToken(String token) {
        return jwtUtil.validateJwtToken(token);
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
