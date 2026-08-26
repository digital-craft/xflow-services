package map.service.xflow_map_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import map.service.xflow_map_service.models.enums.MapVersionStatus;
import map.service.xflow_map_service.models.enums.MapVersionUrgency;

public record MapVersionResponse(
    UUID id,
    UUID tenantId,
    MapVersionStatus status,
    Integer sequenceNumber,
    OffsetDateTime publishedAt,
    UUID publishedBy,
    String changeSummary,
    MapVersionUrgency urgency,
    String[] affectedLayers,
    OffsetDateTime coexistenceUntil,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    UUID createdBy,
    UUID updatedBy
) {}