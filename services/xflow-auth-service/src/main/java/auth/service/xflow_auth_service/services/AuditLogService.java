package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import auth.service.xflow_auth_service.repositories.AuditLogRepository;
import auth.service.xflow_auth_service.dao.AuditLogRequest;
import auth.service.xflow_auth_service.dto.AuditLogResponse;
import auth.service.xflow_auth_service.utils.events.RequestContextHolder;
import auth.service.xflow_auth_service.utils.events.AuditEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

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
}
