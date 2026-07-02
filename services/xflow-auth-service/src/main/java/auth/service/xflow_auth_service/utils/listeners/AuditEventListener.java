package auth.service.xflow_auth_service.utils.listeners;

import auth.service.xflow_auth_service.utils.events.AuditEvent;
import auth.service.xflow_auth_service.models.AuditLog;
import auth.service.xflow_auth_service.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        AuditLog logEntity = AuditLog.builder()
            .serviceSource(event.serviceSource())
            .targetTable(event.targetTable())
            .targetId(event.targetId())
            .actor(event.actor())
            .action(event.action())
            .message(event.message())
            .ipAddress(event.ipAddress())
            .timestamp(event.timestamp())
            .build();

        auditLogRepository.save(logEntity);
    }
}