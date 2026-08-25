package map.service.xflow_map_service.dto;

import org.geojson.GeoJsonObject;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ObstacleResponse(
    UUID id,
    UUID mapVersionId,
    GeoJsonObject geometry,
    Boolean active,
    UUID createdBy,
    UUID updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}