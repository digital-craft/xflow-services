package map.service.xflow_map_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import map.service.xflow_map_service.models.ImportedPlan;

import java.util.Optional;
import java.util.UUID;

public interface ImportedPlanRepository extends JpaRepository<ImportedPlan, UUID> {
    Optional<ImportedPlan> findByIdAndTenantId(UUID id, UUID tenantId);
}