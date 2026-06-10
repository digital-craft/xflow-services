package auth.service.xflow_auth_service.dto;

import java.util.UUID;

public record CreateOperatorResponse(
    UUID id,
    String email,
    String role,
    boolean active,
    long timestamp
) {
}
