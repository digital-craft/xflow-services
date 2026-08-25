package map.service.xflow_map_service.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import map.service.xflow_map_service.dao.CalibratePlanRequest;
import map.service.xflow_map_service.dao.UpdateOpacityRequest;
import map.service.xflow_map_service.dto.CalibrationPointResponse;
import map.service.xflow_map_service.dto.ImportedPlanResponse;
import map.service.xflow_map_service.dto.XflowResponse;
import map.service.xflow_map_service.services.ImportedPlanService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("plans")
@RestController
public class ImportedPlanController {
    
    private final ImportedPlanService planService;

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<XflowResponse<ImportedPlanResponse>> uploadPlan(
        @PathVariable("id") UUID mapVersion,
        @RequestParam("file") MultipartFile file
    ) {
        ImportedPlanResponse response = planService.uploadPlan(mapVersion, file);
        return ResponseEntity.ok(new XflowResponse<>("plan-imported-successfully", response));
    }
    
    @PatchMapping("/{id}/opacity")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<XflowResponse<ImportedPlanResponse>> updateOpacity(
        @PathVariable("id") UUID planId,
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @Valid @RequestBody UpdateOpacityRequest request
    ) {
        ImportedPlanResponse response = planService.updateOpacity(planId, tenantId, request.opacity());
        return ResponseEntity.ok(new XflowResponse<>("plan-opacity-updated-successfully", response));
    }
    
    @PostMapping("/{id}/calibrate")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<XflowResponse<List<CalibrationPointResponse>>> calibratePlan(
        @PathVariable("id") UUID planId,
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @Valid @RequestBody CalibratePlanRequest request
    ) {
        List<CalibrationPointResponse> response = planService.calibratePlan(planId, tenantId, request);
        return ResponseEntity.ok(new XflowResponse<>("plan-calibrated-successfully", response));
    }
}
