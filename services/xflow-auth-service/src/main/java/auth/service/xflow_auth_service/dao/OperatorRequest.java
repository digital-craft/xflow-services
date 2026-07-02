package auth.service.xflow_auth_service.dao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OperatorRequest(
    @NotBlank
    @Email
    String email
) {
}
