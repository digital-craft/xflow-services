package map.service.xflow_map_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import map.service.xflow_map_service.dao.CalibratePlanRequest;
import map.service.xflow_map_service.dao.ControlPointRequest;
import map.service.xflow_map_service.dto.CalibrationPointResponse;
import map.service.xflow_map_service.dto.ImportedPlanResponse;
import map.service.xflow_map_service.models.ImportedPlan;
import map.service.xflow_map_service.models.MapVersion;
import map.service.xflow_map_service.models.CalibrationPoint;
import map.service.xflow_map_service.models.enums.FileType;
import map.service.xflow_map_service.models.enums.CalibrationStatus;
import map.service.xflow_map_service.repositories.CalibrationPointRepository;
import map.service.xflow_map_service.repositories.ImportedPlanRepository;
import map.service.xflow_map_service.repositories.MapVersionRepository;
import map.service.xflow_map_service.utils.exceptions.ResourceNotFoundException;
import map.service.xflow_map_service.utils.mappers.CalibrationPointMapper;
import map.service.xflow_map_service.utils.mappers.ImportedPlanMapper;
import map.service.xflow_map_service.utils.storage.IFileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportedPlanService {

    private final IFileStorage fileStorageService;
    private final ImportedPlanMapper importedPlanMapper;
    private final CalibrationPointMapper calibrationPointMapper;
    private final ImportedPlanRepository importedPlanRepository;
    private final CalibrationPointRepository calibrationPointRepository;
    private final AffineCalibrationService affineCalibrationService;
    private final MapVersionRepository mapVersionRepository;

    public ImportedPlanResponse uploadPlan(UUID mapVersionId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The file cannot be empty.");
        }
        String relativePath = "";
        try {
            String contentType = file.getContentType();
            FileType fileType = FileType.fromMimeType(contentType)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported file format. Only PNG, JPG and PDF are accepted."));

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            relativePath = "plans/" + extension.replace(".", "") + "/" + UUID.randomUUID() + extension;
            String fileUrl = fileStorageService.uploadFile(relativePath, file.getBytes());
            MapVersion mapVersion = mapVersionRepository.findById(mapVersionId)
                    .orElseThrow(() -> new ResourceNotFoundException("map-version-not-found: " + mapVersionId));
            ImportedPlan importedPlan = ImportedPlan.builder()
                .originalFileName(originalFilename)
                .fileUrl(fileUrl)
                .mapVersion(mapVersion)
                .fileType(fileType.name())
                .fileSize(file.getSize())
                .tenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .build();
            ImportedPlan savedPlan = importedPlanRepository.save(importedPlan);
            return importedPlanMapper.toResponse(savedPlan);
        } catch (Exception e) {
            if (relativePath != null) {
                try {
                    log.warn("An error occurred during plan processing. Cleaning up uploaded file: {}", relativePath);
                    fileStorageService.deleteFile(relativePath);
                } catch (Exception cleanupException) {
                    log.error("Failed to delete file during rollback: {}", relativePath, cleanupException);
                }
            }
            throw new RuntimeException("Error occurred while reading the uploaded file", e);
        }
    }

    @Transactional
    public ImportedPlanResponse updateOpacity(UUID planId, UUID tenantId, int opacity) {
        ImportedPlan plan = importedPlanRepository.findByIdAndTenantId(planId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
        plan.setOpacityDefault((short) opacity);
        return importedPlanMapper.toResponse(importedPlanRepository.save(plan));
    }

    @Transactional
    public List<CalibrationPointResponse> calibratePlan(UUID planId, UUID tenantId, CalibratePlanRequest request) {
        ImportedPlan plan = importedPlanRepository.findByIdAndTenantId(planId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan d'imposition non trouvé : " + planId));

        calibrationPointRepository.deleteByPlanId(planId);

        var calibrationResult = affineCalibrationService.calculateResiduals(request.controlPoints());

        List<CalibrationPointResponse> pointResponses = new ArrayList<>();

        for (int i = 0; i < request.controlPoints().size(); i++) {
            ControlPointRequest reqPoint = request.controlPoints().get(i);
            Double error = calibrationResult.residualErrors().get(i);

            CalibrationPoint entity = new CalibrationPoint();
            entity.setPlanId(plan);
            entity.setPixelX(reqPoint.pixelX());
            entity.setPixelY(reqPoint.pixelY());
            entity.setLatitude(reqPoint.latitude());
            entity.setLongitude(reqPoint.longitude());
            entity.setResidualError(error);

            CalibrationPoint saved = calibrationPointRepository.save(entity);

            pointResponses.add(calibrationPointMapper.toResponse(saved));
        }

        plan.setCalibrationStatus(CalibrationStatus.CALIBRATED);
        importedPlanRepository.save(plan);

        return pointResponses;
    }
}