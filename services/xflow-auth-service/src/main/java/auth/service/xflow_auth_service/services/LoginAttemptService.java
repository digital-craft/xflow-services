package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import auth.service.xflow_auth_service.config.JwtConfig;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.utils.events.AuditEvent;
import auth.service.xflow_auth_service.utils.events.RequestContextHolder;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(User user) {
        String ip = RequestContextHolder.getClientIp();
        String actorEmail = RequestContextHolder.getUserEmail();
        int newAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newAttempts);
        if (newAttempts >= jwtConfig.maxFailedAttempts()) {
            eventPublisher.publishEvent(new AuditEvent(
                "xflow-auth-service",
                "users",
                user.getId().toString(),
                user.getEmail(),
                "USER_LOGIN_FAILED",
                "Account locked due to too many failed login attempts.",
                ip
            ));
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(jwtConfig.lockoutDurationMs() / 60000));
        }
        userRepository.save(user);
    }

    public boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) return false;
        return user.getLockedUntil().isAfter(OffsetDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}
