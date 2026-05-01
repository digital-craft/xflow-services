package auth.service.xflow_auth_service.services;

import auth.service.xflow_auth_service.config.JwtConfig;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(User user) {
        int newAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newAttempts);
        if (newAttempts >= jwtConfig.maxFailedAttempts()) {
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
