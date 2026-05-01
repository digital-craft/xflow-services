package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.dao.LoginRequest;
import auth.service.xflow_auth_service.dto.LoginResponse;
import auth.service.xflow_auth_service.config.RsaKeyConfig;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RsaKeyConfig rsaKeyConfig;
    private final LoginAttemptService loginAttemptService;

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
        return new LoginResponse(
                token,
                "Bearer",
                user.getRole().name(),
                rsaKeyConfig.expirationMs()
        );
    }
}