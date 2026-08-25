package map.service.xflow_map_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.LineString;

import lombok.experimental.SuperBuilder;
import map.service.xflow_map_service.models.bases.DateBaseModel;

import java.util.UUID;

@Entity
@Table(name = "obstacles")
@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
public class Obstacle extends DateBaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_version_id", nullable = false)
    private MapVersion mapVersion;
    
    @Column(name = "geom", nullable = false, columnDefinition = "geometry(LineString,4326)")
    private LineString geom;
    
    @NotNull
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

}
