package auth.service.xflow_auth_service.utils.events;

import java.time.OffsetDateTime;

public record AuditEvent(
    String serviceSource,
    String targetTable,
    String targetId,
    String actor,
    String action,
    String message,
    String ipAddress,
    OffsetDateTime timestamp
) {
    public AuditEvent(
        String serviceSource,
        String targetTable,
        String targetId,
        String actor,
        String action,
        String message,
        String ipAddress
    ) {
        this(
            serviceSource,
            targetTable,
            targetId,
            actor,
            action,
            message,
            ipAddress,
            OffsetDateTime.now()
        );
    }
}
