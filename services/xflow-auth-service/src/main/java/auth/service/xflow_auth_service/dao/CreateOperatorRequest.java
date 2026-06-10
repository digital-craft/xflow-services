package auth.service.xflow_auth_service.dao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateOperatorRequest(
    @NotBlank
    @Email
    String email
) {
}
