package map.service.xflow_map_service.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import map.service.xflow_map_service.dto.ImportedPlanResponse;
import map.service.xflow_map_service.dto.XflowResponse;
import map.service.xflow_map_service.services.ImportedPlanService;

@RequiredArgsConstructor
@RequestMapping("plans")
@RestController
public class ImportedPlanController {
    
    private final ImportedPlanService planService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<XflowResponse<ImportedPlanResponse>> uploadPlan(@RequestParam("file") MultipartFile file) {
        ImportedPlanResponse response = planService.uploadPlan(file);
        return ResponseEntity.ok(new XflowResponse<>("plan-imported-successfully", response));
    }
}
