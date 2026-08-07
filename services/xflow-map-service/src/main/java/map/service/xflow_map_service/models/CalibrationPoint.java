package map.service.xflow_map_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import map.service.xflow_map_service.models.bases.DateBaseModel;

import java.util.UUID;


@Entity
@Table(name = "calibration_points")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class CalibrationPoint extends DateBaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private ImportedPlan planId;

    @NotNull
    @Column(name = "pixel_x", nullable = false)
    private Double pixelX;

    @NotNull
    @Column(name = "pixel_y", nullable = false)
    private Double pixelY;

    @NotNull
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "residual_error")
    private Double residualError;
}
