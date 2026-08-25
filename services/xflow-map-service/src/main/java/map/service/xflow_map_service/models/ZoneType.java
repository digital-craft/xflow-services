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

import java.util.UUID;

@Entity
@Table(name = "zone_types")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class ZoneType extends DateBaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @NotNull
    @Column(name = "code", length = 100, nullable = false)
    private String code;
    
    @NotNull
    @Column(name = "label", length = 100, nullable = false)
    private String label;
    
    @NotNull
    @Column(name = "color", length = 100, nullable = false)
    private String color;
    
    @NotNull
    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
}
