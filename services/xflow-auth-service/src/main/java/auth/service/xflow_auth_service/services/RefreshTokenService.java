package auth.service.xflow_auth_service.services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auth.service.xflow_auth_service.models.RefreshToken;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.repositories.RefreshTokenRepository;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.config.RsaKeyConfig;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RsaKeyConfig rsaKeyConfig;

    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("user-not-found"));
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryAt(Instant.now().plusMillis(rsaKeyConfig.refreshExpirationMs()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BadCredentialsException("expired-refresh-token");
        }
        return token;
    }

    @Transactional
    public void revokeByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}