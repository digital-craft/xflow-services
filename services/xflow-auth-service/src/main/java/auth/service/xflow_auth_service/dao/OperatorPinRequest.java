package auth.service.xflow_auth_service.dao;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record OperatorPinRequest(
    @NotBlank 
    @Size(min = 6, max = 6) 
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN must be numeric (06 digits)")
    String pin
) {}