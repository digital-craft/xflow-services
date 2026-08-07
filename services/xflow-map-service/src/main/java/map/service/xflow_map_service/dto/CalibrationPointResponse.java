package map.service.xflow_map_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CalibrationPointResponse(
    UUID id,
    UUID planId,
    Double pixelX,
    Double pixelY,
    Double latitude,
    Double longitude,
    Double residualError,
    UUID createdBy,
    UUID updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

}
