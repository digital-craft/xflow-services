package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;

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
import auth.service.xflow_auth_service.utils.events.AuditEvent;
import auth.service.xflow_auth_service.utils.security.SecurityUtils;
import auth.service.xflow_auth_service.utils.mappers.UserMapper;
import auth.service.xflow_auth_service.utils.security.AnonymousRateLimiter;
import auth.service.xflow_auth_service.utils.events.RequestContextHolder;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String ip = RequestContextHolder.getClientIp();
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
        Map<String, String> emailFields = Map.of(
            "login", request.email(),
            "ip_address", ip,
            "primary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        emailService.sendTemplateEmailSync(
            null,
            request.email(),
            "Welcome to XFlow — Your carrier access",
            "classpath:templates/login-confirmed.html",
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            user.getId().toString(),
            user.getEmail(),
            "USER_LOGIN",
            "User logged successfully",
            ip
        ));
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
        String ip = RequestContextHolder.getClientIp();
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
        Map<String, String> emailFields = Map.of(
            "login", user.getEmail(),
            "ip_address", ip,
            "primary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        emailService.sendTemplateEmailSync(
            null,
            user.getEmail(),
            "Welcome to XFlow — Your carrier access",
            "classpath:templates/login-confirmed.html",
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            user.getId().toString(),
            user.getEmail(),
            "USER_LOGIN",
            "User logged successfully",
            ip
        ));
        return new LoginResponse(
                token,
                refreshToken.getToken(),
                "Bearer2",
                user.getRole().name(),
                rsaKeyConfig.expirationMs()
        );
    }

    @Transactional
    public LoginResponse loginAnonymous(String fingerprint) {
        String ip = RequestContextHolder.getClientIp();
        if (!anonymousLimiter.isAllowed(fingerprint)) {
            throw new LockedException("too-many-attempts");
        }
        AnonymousToken tokenEntity = new AnonymousToken();
        tokenEntity.setExpiresAt(OffsetDateTime.now().plusHours(rsaKeyConfig.expirationMs()));
        AnonymousToken savedToken = anonymousTokenRepository.save(tokenEntity);
        anonymousLimiter.recordAttempt(fingerprint);
        String jwt = jwtService.generateAnonymousToken(savedToken.getId().toString(), "PARTICIPANT");
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "anonymous_tokens",
            savedToken.getId().toString(),
            "unknown",
            "USER_LOGIN",
            "User logged successfully",
            ip
        ));
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
    public void logout(LogoutRequest request) {
        String actorEmail = RequestContextHolder.getUserEmail();
        String ip = RequestContextHolder.getClientIp();
        String requestToken = request.refreshToken();
        refreshTokenRepository.findByToken(requestToken)
            .ifPresent(token -> refreshTokenRepository.delete(token));
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            actorEmail != null ? actorEmail : "unknown",
            actorEmail != null ? actorEmail : "unknown",
            "USER_LOGOUT",
            "User logged out successfully",
            ip
        ));
    }

    @Transactional
    public UserResponse regenerateOperatorCredentials(@NonNull UUID id) {
        String ip = RequestContextHolder.getClientIp();
        String actorEmail = RequestContextHolder.getUserEmail();
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
        Map<String, String> emailFields = Map.of(
            "login", operator.getEmail(),
            "password", rawPassword,
            "code", rawPin,
            "admin", actorEmail,
            "primary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        emailService.sendTemplateEmailSync(
            null,
            operator.getEmail(),
            "Your credentials have been reset by an administrator",
            "classpath:templates/reset-credential.html",
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            operator.getId().toString(),
            actorEmail != null ? actorEmail : "unknown",
            "USER_REGENERATE_CREDENTIALS",
            "User credentials regenerated successfully",
            ip
        ));
        return userMapper.toResponse(operator);
    }

    @Transactional
    public UserResponse changePassword(String email, ChangePasswordRequest request) {
        String ip = RequestContextHolder.getClientIp();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user-not-found"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("invalid-old-password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChanged(true);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        Map<String, String> emailFields = Map.of(
            "login", user.getEmail(),
            "ip_address", ip,
            "primary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        emailService.sendTemplateEmailSync(
            null,
            user.getEmail(),
            "Your password has been changed",
            "classpath:templates/password-updated.html",
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            user.getId().toString(),
            user.getEmail(),
            "USER_CHANGE_PASSWORD",
            "User changed password successfully",
            ip
        ));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse changePin(String email, ChangePinRequest request) {
        String ip = RequestContextHolder.getClientIp();
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
        Map<String, String> emailFields = Map.of(
            "login", user.getEmail(),
            "ip_address", ip,
            "primary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        emailService.sendTemplateEmailSync(
            null,
            user.getEmail(),
            "Your password has been changed",
            "classpath:templates/pin-updated.html",
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            user.getId().toString(),
            user.getEmail(),
            "USER_CHANGE_PIN",
            "User changed pin successfully",
            ip
        ));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse getCurrentUserInfo(String email) {
        String ip = RequestContextHolder.getClientIp();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user-not-found"));
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            user.getId().toString(),
            user.getEmail(),
            "USER_INFO_RETRIEVED",
            "User info retrieved successfully",
            ip
        ));
        return userMapper.toResponse(user);
    }
}