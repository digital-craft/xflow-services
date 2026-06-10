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
import auth.service.xflow_auth_service.dao.LoginPinRequest;
import auth.service.xflow_auth_service.dao.RefreshTokenRequest;
import auth.service.xflow_auth_service.dao.LogoutRequest;
import auth.service.xflow_auth_service.dao.ChangePasswordRequest;
import auth.service.xflow_auth_service.dao.ChangePinRequest;
import auth.service.xflow_auth_service.dto.LoginResponse;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.config.RsaKeyConfig;
import auth.service.xflow_auth_service.utils.security.SecurityUtils;
import auth.service.xflow_auth_service.utils.mappers.UserMapper;
import auth.service.xflow_auth_service.utils.security.AnonymousRateLimiter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

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
    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;

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
    public LoginResponse loginOperator(LoginPinRequest request) {
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
    public UserResponse regenerateOperatorCredentials(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("operator-not-found");
        }
        String rawPassword = securityUtils.generateRandomPassword();
        String rawPin = securityUtils.generateRandomPin();
        User operator = userRepository.findById(id).orElseThrow(() -> new RuntimeException("operator-not-found"));
        operator.setPassword(passwordEncoder.encode(rawPassword));
        operator.setPin(passwordEncoder.encode(rawPin));
        operator.setPasswordChanged(false);
        operator.setPinChanged(false);
        userRepository.save(operator);
        securityUtils.sendCredentialsEmail(operator.getEmail(), rawPassword, rawPin);
        return userMapper.toResponse(operator);
    }

    @Transactional
    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user-not-found"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("invalid-old-password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChanged(true);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse changePin(String email, ChangePinRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user-not-found"));
        if (!passwordEncoder.matches(request.oldPin(), user.getPin())) {
            throw new RuntimeException("invalid-old-pin");
        }
        if (user.getRole() != UserRole.ROLE_OPERATOR) {
            throw new RuntimeException("only-operator-can-change-pin");
        }
        user.setPin(passwordEncoder.encode(request.newPin()));
        user.setPinChanged(true);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        return userMapper.toResponse(user);
    }
    
    @Transactional
    public void logout(LogoutRequest request) {
        String requestToken = request.refreshToken();
        refreshTokenRepository.findByToken(requestToken)
            .ifPresent(token -> refreshTokenRepository.delete(token));
    }
}