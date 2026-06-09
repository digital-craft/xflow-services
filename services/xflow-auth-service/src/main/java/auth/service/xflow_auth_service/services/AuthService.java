package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.RefreshToken;
import auth.service.xflow_auth_service.models.AnonymousToken;
import auth.service.xflow_auth_service.models.enums.UserRole;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.repositories.AnonymousTokenRepository;
import auth.service.xflow_auth_service.repositories.RefreshTokenRepository;
import auth.service.xflow_auth_service.dao.LoginRequest;
import auth.service.xflow_auth_service.dao.OperatorPinRequest;
import auth.service.xflow_auth_service.dao.RefreshTokenRequest;
import auth.service.xflow_auth_service.dao.LogoutRequest;
import auth.service.xflow_auth_service.dto.LoginResponse;
import auth.service.xflow_auth_service.dto.LogoutResponse;
import auth.service.xflow_auth_service.config.RsaKeyConfig;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RsaKeyConfig rsaKeyConfig;
    private final LoginAttemptService loginAttemptService;
    private final AnonymousTokenRepository anonymousTokenRepository;
    private final AnonymousRateLimiter anonymousLimiter;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("invalid-credentials"));
        if (loginAttemptService.isAccountLocked(user)) {
            throw new LockedException("too-many-attempts");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            loginAttemptService.incrementFailedAttempts(user);
            throw new BadCredentialsException("invalid-credentials");
        }
        loginAttemptService.resetAttempts(user);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new LoginResponse(
            token,
            refreshToken.getToken(),
            "Bearer",
            user.getRole().name(),
            rsaKeyConfig.expirationMs()
        );
    }

    @Transactional
    public LoginResponse loginOperator(OperatorPinRequest request) {
        List<User> users = userRepository.findAllByRole(UserRole.ROLE_OPERATOR);
        User user = users.stream()
            .filter(
                u -> u.getPin() != null && 
                passwordEncoder.matches(request.pin(), u.getPin()))
            .findFirst()
            .orElseThrow(() -> new BadCredentialsException("invalid-credentials"));
        if (loginAttemptService.isAccountLocked(user)) {
            throw new LockedException("account-locked");
        }
        loginAttemptService.resetAttempts(user);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new LoginResponse(
                token,
                refreshToken.getToken(),
                "Bearer",
                user.getRole().name(),
                rsaKeyConfig.expirationMs()
        );
    }

    @Transactional
    public LoginResponse loginAnonymous(String fingerprint) {
        if (!anonymousLimiter.isAllowed(fingerprint)) {
            throw new LockedException("too-many-attempts");
        }
        AnonymousToken tokenEntity = new AnonymousToken();
        tokenEntity.setExpiresAt(OffsetDateTime.now().plusHours(rsaKeyConfig.expirationMs()));
        AnonymousToken savedToken = anonymousTokenRepository.save(tokenEntity);
        anonymousLimiter.recordAttempt(fingerprint);
        String jwt = jwtService.generateAnonymousToken(savedToken.getId().toString(), "PARTICIPANT");
        return new LoginResponse(
                jwt,
                "",
                "Bearer",
                "PARTICIPANT",
                rsaKeyConfig.expirationMs()
        );
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.refreshToken();
        return refreshTokenRepository.findByToken(requestToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = token.getUser();
                    String accessToken = jwtService.generateToken(user);
                    return new LoginResponse(
                        accessToken,
                        requestToken,
                        "Bearer",
                        user.getRole().name(), 
                        rsaKeyConfig.expirationMs()
                    );
                })
                .orElseThrow(() -> new BadCredentialsException("unknown-token"));
    }
    
    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        String requestToken = request.refreshToken();
        return refreshTokenRepository.findByToken(requestToken)
            .map(token -> {
                    refreshTokenRepository.delete(token);
                    return new LogoutResponse("logout-success");
            })
            .orElseThrow(() -> new BadCredentialsException("unknown-token"));
    }
}