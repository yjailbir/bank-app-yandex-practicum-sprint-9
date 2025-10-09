package ru.yjailbir.accountsservice.service;

import ru.yjailbir.commonservice.dto.request.LoginRequestDto;
import ru.yjailbir.commonservice.dto.request.PasswordChangeDto;
import ru.yjailbir.commonservice.dto.request.PasswordChangeDtoWithToken;
import ru.yjailbir.commonservice.dto.request.RegisterRequestDto;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;

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
        UserEntity user = userRepository.findByLogin(dto.login())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не существует!"));

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
            //По идее это никогда не выбросится, потому что токен нельзя скомпрометировать, потому что он хранится на сервере
            throw new IllegalArgumentException("Пользователь не существует!");
        }
    }

    public String validateToken(String token) {
        return jwtUtil.validateJwtToken(token);
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
