package auth.service.xflow_auth_service.dao;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogRequest (
    @NotBlank
    String serviceSource,
    
    @NotBlank
    String targetTable,
    
    @NotBlank
    String targetId,
    
    @NotBlank
    String action,
    
    @NotBlank
    String message,
    
    @NotBlank
    String ipAddress
) {
}
