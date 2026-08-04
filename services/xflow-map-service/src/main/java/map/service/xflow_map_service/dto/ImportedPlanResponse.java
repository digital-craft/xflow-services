package map.service.xflow_map_service.dto;

import map.service.xflow_map_service.models.enums.CalibrationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ImportedPlanResponse (
    UUID id,
    UUID tenantId,
    String originalFileName,
    String fileUrl,
    String fileType,
    long fileSize,
    int opacityDefault,
    CalibrationStatus calibrationStatus,
    UUID createdBy,
    UUID updatedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}