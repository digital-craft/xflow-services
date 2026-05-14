package auth.service.xflow_auth_service.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.config.RsaKeyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RsaKeyConfig rsaKeyConfig;

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("role", user.getRole().name())
            .claim("email", user.getEmail())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(rsaKeyConfig.expirationMs())))
            .signWith(rsaKeyConfig.privateKey())
            .compact();
    }

    public String generateAnonymousToken(String anonymousId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(anonymousId)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(rsaKeyConfig.expirationMs())))
            .signWith(rsaKeyConfig.privateKey())
            .compact();
    }

}
