package map.service.xflow_map_service.dao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateOpacityRequest(
    @NotNull(message = "The opacity value is required")
    @Min(value = 0, message = "The opacity must be at least 0%")
    @Max(value = 100, message = "The opacity cannot exceed 100%")
    Integer opacity
) {

}
