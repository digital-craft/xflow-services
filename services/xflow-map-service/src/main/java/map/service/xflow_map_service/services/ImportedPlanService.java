package map.service.xflow_map_service.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import map.service.xflow_map_service.dto.ImportedPlanResponse;
import map.service.xflow_map_service.models.ImportedPlan;
import map.service.xflow_map_service.models.enums.FileType;
import map.service.xflow_map_service.repositories.ImportedPlanRepository;
import map.service.xflow_map_service.utils.mappers.ImportedPlanMapper;
import map.service.xflow_map_service.utils.storage.IFileStorage;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportedPlanService {

    private final IFileStorage fileStorageService;
    private final ImportedPlanMapper importedPlanMapper;
    private final ImportedPlanRepository importedPlanRepository;

    public ImportedPlanResponse uploadPlan(MultipartFile file) {
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
            ImportedPlan importedPlan = ImportedPlan.builder()
                .originalFileName(originalFilename)
                .fileUrl(fileUrl)
                .fileType(fileType.name())
                .fileSize(file.getSize())
                .tenantId(UUID.randomUUID())
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
}