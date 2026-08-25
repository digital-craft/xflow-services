package map.service.xflow_map_service.dao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.geojson.GeoJsonObject;

import java.util.UUID;

public record CreateZoneRequest(
    @NotNull UUID zoneType,
    @NotBlank String name,
    @NotNull @Min(1) Integer capacityMax,
    @NotNull GeoJsonObject geometry
) {

}
