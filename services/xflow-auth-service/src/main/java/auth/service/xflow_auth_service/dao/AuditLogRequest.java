package auth.service.xflow_auth_service.dao;

import jakarta.validation.constraints.NotBlank;

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
