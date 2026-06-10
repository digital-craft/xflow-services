package auth.service.xflow_auth_service.dto;

public record CreateOperatorResponse(
    String email,
    String role,
    long timestamp
) {
}
