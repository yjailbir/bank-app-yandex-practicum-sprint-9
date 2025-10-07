package ru.yjailbir.accountsservice.service;

import ru.yjailbir.commonservice.dto.request.LoginRequestDto;
import ru.yjailbir.commonservice.dto.request.RegisterRequestDto;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;

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
        if (userRepository.findByLogin(dto.login()).isEmpty()) {
            userRepository.save(new UserEntity(
                    dto.login(),
                    hashPassword(dto.password()),
                    dto.surname(),
                    dto.name()
            ));
        } else {
            throw new IllegalArgumentException("Имя пользователя занято!");
        }
    }

    public String loginUser(LoginRequestDto dto) {
        UserEntity user = userRepository.findByLogin(dto.login())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не существует!"));

        if (!verifyPassword(dto.password(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный пароль!");
        }

        return jwtUtil.generateJwtToken(user);
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
