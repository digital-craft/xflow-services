package map.service.xflow_map_service.dao;

import jakarta.validation.constraints.NotNull;
import org.geojson.GeoJsonObject;

import java.util.UUID;

public record CreateObstacleRequest(
        @NotNull UUID mapVersionId,
        @NotNull GeoJsonObject geometry
) {}