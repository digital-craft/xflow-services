package map.service.xflow_map_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String error,
        String message,
        int status,
        Instant timestamp,
        Map<String, String> validationErrors
) {
    public ErrorResponse(String error, String message, int status) {
        this(error, message, status, Instant.now(), null);
    }

    public ErrorResponse(String error, String message, int status, Map<String, String> validationErrors) {
        this(error, message, status, Instant.now(), validationErrors);
    }
}