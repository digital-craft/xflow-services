package auth.service.xflow_auth_service.services;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import auth.service.xflow_auth_service.repositories.AuditLogRepository;
import auth.service.xflow_auth_service.dao.AuditLogRequest;
import auth.service.xflow_auth_service.dto.AuditLogResponse;
import auth.service.xflow_auth_service.utils.events.RequestContextHolder;
import auth.service.xflow_auth_service.utils.events.AuditEvent;
import auth.service.xflow_auth_service.utils.mappers.AuditLogMapper;
import auth.service.xflow_auth_service.models.AuditLog;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogMapper auditLogMapper;

    @Transactional
    public void logEvent(AuditLogRequest request) {
        String actorEmail = RequestContextHolder.getUserEmail();
        eventPublisher.publishEvent(new AuditEvent(
            request.serviceSource(),
            request.targetTable(),
            request.targetId(),
            actorEmail != null ? actorEmail : "unknown",
            request.action(),
            request.message(),
            request.ipAddress()
        ));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(
            String serviceSource,
            String targetTable,
            String actor,
            String action,
            Pageable pageable
    ) {
        Specification<AuditLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (serviceSource != null && !serviceSource.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("serviceSource"), serviceSource.trim()));
            }
            if (targetTable != null && !targetTable.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("targetTable"), targetTable.trim()));
            }
            if (actor != null && !actor.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("actor"), actor.trim()));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action.trim()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> auditLogPage = auditLogRepository.findAll(spec, pageable);
        return auditLogPage.map(auditLogMapper::toResponse);
    }
}
