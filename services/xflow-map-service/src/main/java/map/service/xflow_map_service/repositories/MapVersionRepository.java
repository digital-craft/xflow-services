package map.service.xflow_map_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import map.service.xflow_map_service.models.MapVersion;
import java.util.Optional;

import java.util.UUID;

public interface MapVersionRepository extends JpaRepository<MapVersion, UUID> {
    Optional<MapVersion> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<MapVersion> findByTenantId(UUID tenantId, Pageable pageable);

    @Query("SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM MapVersion m WHERE m.tenantId = :tenantId")
    Integer findMaxSequenceNumberByTenantId(@Param("tenantId") UUID tenantId);
}
