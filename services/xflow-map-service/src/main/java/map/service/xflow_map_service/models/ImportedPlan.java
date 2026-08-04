package map.service.xflow_map_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import map.service.xflow_map_service.models.bases.DateBaseModel;
import map.service.xflow_map_service.models.enums.CalibrationStatus;

import java.util.UUID;

@Entity
@Table(name = "imported_plans")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class ImportedPlan extends DateBaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @NotNull
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotNull    
    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @NotNull
    @Column(name = "file_type", nullable = false)
    private String fileType;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Min(0)
    @Max(100)
    @Builder.Default
    @Column(name = "opacity_default", nullable = false)
    private Short opacityDefault = 60;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "calibration_status", nullable = false, length = 20)
    private CalibrationStatus calibrationStatus = CalibrationStatus.PENDING;

}