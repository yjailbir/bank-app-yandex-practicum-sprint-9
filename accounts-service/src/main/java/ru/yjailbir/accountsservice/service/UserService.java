package ru.yjailbir.accountsservice.service;

import dto.request.LoginRequestDto;
import dto.request.RegisterRequestDto;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yjailbir.accountsservice.entity.UserEntity;
import ru.yjailbir.accountsservice.repository.UserRepository;
import ru.yjailbir.accountsservice.security.JwtUtil;

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
        if (userRepository.findByLogin(dto.login()).isEmpty()) {
            userRepository.save(new UserEntity(
                    dto.login(),
                    hashPassword(dto.password()),
                    dto.surname(),
                    dto.name()
            ));
        } else {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    public String loginUser(LoginRequestDto dto) {
        Optional<UserEntity> user = userRepository.findByLogin(dto.login());
        if (user.isPresent()) {
            if (verifyPassword(dto.password(), user.get().getPassword())) {
                return jwtUtil.generateJwtToken(user.get());
            }
        } else {
            throw new IllegalArgumentException("Username does not exist");
        }
        return null;
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
