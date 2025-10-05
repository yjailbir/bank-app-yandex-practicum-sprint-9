package ru.yjailbir.accountsservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yjailbir.accountsservice.entity.UserEntity;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.token-expiration}")
    private int jwtExpirationMs;
    @Value("${jwt.secret}")
    private String jwtSecret;

    public String validateJwtToken(String authToken) {
        String error = "";
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return error;
        } catch (SignatureException e) {
            error = "Invalid JWT signature";
        } catch (MalformedJwtException e) {
            error = "Invalid JWT token";
        } catch (ExpiredJwtException e) {
            error = "Expired JWT token";
        } catch (UnsupportedJwtException e) {
            error = "Unsupported JWT token";
        } catch (IllegalArgumentException e) {
            error = "JWT claims string is empty";
        }

        return error;
    }

    public String generateJwtToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getLogin())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getLoginFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }
}
