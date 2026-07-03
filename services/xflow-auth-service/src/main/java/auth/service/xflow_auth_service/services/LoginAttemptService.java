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
import java.util.Map;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final EmailService emailService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(User user) {
        Map<String, String> emailFields;
        String ip = RequestContextHolder.getClientIp();
        String actorEmail = RequestContextHolder.getUserEmail();
        int newAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newAttempts);
        OffsetDateTime now = OffsetDateTime.now();
        String offset = now.getOffset().getId().equals("Z") ? "+00:00" : now.getOffset().getId();
        if (newAttempts >= jwtConfig.maxFailedAttempts()) {
            long delayInMinutes = jwtConfig.lockoutDurationMs() / 60000;
            OffsetDateTime delayTime = now.plusMinutes(delayInMinutes);
            emailFields = Map.of(
                "login", user.getEmail(),
                "attempts", String.valueOf(newAttempts),
                "last_attempt", now.format(timeFormatter),
                "ip_address", ip,
                "delay", String.valueOf(delayInMinutes / 60),
                "primary_button", "https://www.google.com",
                "secondary_button", "https://www.google.com",
                "privacy", "https://www.google.com",
                "terms", "https://www.google.com"
            );
            emailService.sendTemplateEmailSync(
                null,
                user.getEmail(),
                "Votre compte XFlow a été temporairement bloqué",
                "classpath:templates/account-locked.html",
                emailFields
            );
            eventPublisher.publishEvent(new AuditEvent(
                "xflow-auth-service",
                "users",
                user.getId().toString(),
                user.getEmail(),
                "USER_LOGIN_FAILED",
                "Account locked due to too many failed login attempts.",
                ip
            ));
            user.setLockedUntil(delayTime);
        }
        emailFields = Map.of(
            "login", user.getEmail(),
            "date_time", now.format(dateTimeFormatter),
            "timezone_offset", offset,
            "ip_address", ip,
            "attempt_count", String.valueOf(newAttempts),
            "max_attempts", String.valueOf(jwtConfig.maxFailedAttempts()),
            "primary_button", "https://www.google.com",
            "secondary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        emailService.sendTemplateEmailSync(
            null,
            user.getEmail(),
            "Votre compte XFlow a été temporairement bloqué",
            "classpath:templates/login-attempt.html",
            emailFields
        );
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
