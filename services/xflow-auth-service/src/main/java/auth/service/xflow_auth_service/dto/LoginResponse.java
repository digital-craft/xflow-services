package auth.service.xflow_auth_service.dto;

public record LoginResponse(
    String token,
    String tokenType,
    String role,
    long expiresIn
) {
}
