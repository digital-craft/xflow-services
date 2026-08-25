package map.service.xflow_map_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import map.service.xflow_map_service.models.MapVersion;
import java.util.Optional;

import java.util.UUID;

public interface MapVersionRepository extends JpaRepository<MapVersion, UUID> {
    Optional<MapVersion> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<MapVersion> findByTenantId(UUID tenantId);
}
