package map.service.xflow_map_service.dao;

import map.service.xflow_map_service.models.enums.MapVersionUrgency;

import java.time.OffsetDateTime;

public class MapVersionDAO {

    public record CreateMapVersionRequest(
        String changeSummary,
        MapVersionUrgency urgency,
        String[] affectedLayers
    ) {}

    public record UpdateMapVersionRequest(
        String changeSummary,
        MapVersionUrgency urgency,
        String[] affectedLayers,
        OffsetDateTime coexistenceUntil
    ) {}

    public record PublishMapVersionRequest(
        OffsetDateTime coexistenceUntil
    ) {}
    
}
