package ru.yjailbir.accountsservice.service;

import ru.yjailbir.commonservice.dto.request.*;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;
import ru.yjailbir.commonservice.dto.response.UserDataResponseDto;

import java.time.LocalDate;
import java.time.Period;
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
        return new UserDataResponseDto("ok", user.getLogin(), user.getName(), user.getSurname());
    }

    public void updateUser(UserEditDtoWithToken dto) {
        Optional<UserEntity> userOptional = userRepository.findByLogin(jwtUtil.getLoginFromJwtToken(dto.token()));
        if (userOptional.isPresent()) {
            UserEntity userEntity = userOptional.get();
            if(dto.name() != null) {
                userEntity.setName(dto.name());
            }
            if(dto.surname() != null) {
                userEntity.setSurname(dto.surname());
            }
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
