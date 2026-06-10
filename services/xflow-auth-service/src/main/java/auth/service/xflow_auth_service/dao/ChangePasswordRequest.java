package auth.service.xflow_auth_service.dao;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
    @NotBlank
    String oldPassword,

    @NotBlank
    @Size(min = 8, message = "The new password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&\\.])[A-Za-z\\d@$!%*?&\\.]{8,}$",
        message = "The new password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&.)"
    )
    String newPassword
) {
}
