package auth.service.xflow_auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(
    long maxFailedAttempts,
    long lockoutDurationMs
) {
}