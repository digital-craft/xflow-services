package auth.service.xflow_auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.AuditLogService;
import auth.service.xflow_auth_service.dao.AuditLogRequest;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logEvent(@RequestBody AuditLogRequest request) {
        auditLogService.logEvent(request);
        return ResponseEntity.ok().build();
    }
}
