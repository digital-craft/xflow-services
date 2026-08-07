package map.service.xflow_map_service.dao;

import jakarta.validation.constraints.NotNull;

public record ControlPointRequest(
    @NotNull(message = "Required field: pixelX")
    Double pixelX,

    @NotNull(message = "Required field: pixelY")
    Double pixelY,

    @NotNull(message = "Required field: latitude")
    Double latitude,

    @NotNull(message = "Required field: longitude")
    Double longitude
) {

}
