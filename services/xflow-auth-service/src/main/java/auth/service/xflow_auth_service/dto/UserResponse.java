package auth.service.xflow_auth_service.dto;

import auth.service.xflow_auth_service.models.enums.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse (
    UUID id,
    String email,
    UserRole role,
    boolean active,
    boolean passwordChanged,
    boolean pinChanged,
    OffsetDateTime lastLoginAt,
    OffsetDateTime lockedUntil,
    Integer failedAttempts
) {
}
