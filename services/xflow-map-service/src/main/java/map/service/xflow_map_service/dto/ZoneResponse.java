package map.service.xflow_map_service.dto;

import org.geojson.GeoJsonObject;

import map.service.xflow_map_service.models.MapVersion;
import map.service.xflow_map_service.models.ZoneType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ZoneResponse(
    UUID id,
    MapVersion mapVersion,
    ZoneType zoneType,
    String name,
    Integer capacityMax,
    GeoJsonObject geom,
    Boolean active,
    UUID createdBy,
    UUID updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    
}
