package auth.service.xflow_auth_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse (
    UUID id,
    String serviceSource,
    String targetTable,
    String targetId,
    String actor,
    String action,
    String message,
    String ipAddress,
    OffsetDateTime timestamp
) {
}
