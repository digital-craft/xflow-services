package map.service.xflow_map_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import map.service.xflow_map_service.models.bases.DateBaseModel;
import map.service.xflow_map_service.models.enums.MapVersionStatus;
import map.service.xflow_map_service.models.enums.MapVersionUrgency;

import java.util.UUID;

@Entity
@Table(name = "map_versions")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class MapVersion extends DateBaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private MapVersionStatus status = MapVersionStatus.DRAFT;

    @NotNull
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "published_at", nullable = false)
    private java.time.OffsetDateTime publishedAt;

    @Column(name = "published_by", nullable = false)
    private UUID publishedBy;

    @Column(name = "change_summary")
    private String changeSummary;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false, length = 10)
    private MapVersionUrgency urgency = MapVersionUrgency.NORMAL;

    @NotNull
    @Builder.Default
    @Column(name = "affected_layers", nullable = false)
    private String[] affectedLayers = new String[0];

    @Column(name = "coexistence_until")
    private java.time.OffsetDateTime coexistenceUntil;

}
