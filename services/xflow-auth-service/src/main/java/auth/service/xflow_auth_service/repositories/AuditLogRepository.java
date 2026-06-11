package auth.service.xflow_auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import auth.service.xflow_auth_service.models.AuditLog;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    Optional<AuditLog> findById(UUID id);
}
