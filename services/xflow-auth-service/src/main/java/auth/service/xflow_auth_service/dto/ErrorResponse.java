package auth.service.xflow_auth_service.dto;

public record ErrorResponse(
    String message,
    long timestamp,
    int status
) {
}
