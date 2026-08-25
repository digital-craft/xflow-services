package map.service.xflow_map_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.geojson.Geometry;
import lombok.experimental.SuperBuilder;
import map.service.xflow_map_service.models.bases.DateBaseModel;

import java.util.UUID;

@Entity
@Table(name = "zones")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class Zone extends DateBaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_version_id", nullable = false)
    private MapVersion mapVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_type_id", nullable = false)
    private ZoneType zoneType;

    @NotNull
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @NotNull
    @Column(name = "capacity_max", nullable = false)
    private Integer capacityMax;

    @NotNull
    @Column(name = "geom", nullable = false, columnDefinition = "geometry(Geometry,4326)")
    private Geometry<?> geom;

    @NotNull
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

}
