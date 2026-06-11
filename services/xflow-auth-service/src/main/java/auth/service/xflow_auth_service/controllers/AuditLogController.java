package auth.service.xflow_auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.AuditLogService;
import auth.service.xflow_auth_service.dao.AuditLogRequest;
import auth.service.xflow_auth_service.dto.ApiResponse;
import auth.service.xflow_auth_service.dto.AuditLogResponse;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> logEvent(@RequestBody AuditLogRequest request) {
        auditLogService.logEvent(request);
        return ResponseEntity.ok(new ApiResponse<>("audit-log-saved-successfully", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String serviceSource,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AuditLogResponse> logs = auditLogService.getAuditLogs(
            serviceSource, targetTable, actor, action, pageable
        );
        return ResponseEntity.ok(new ApiResponse<>("audit-logs-retrieved-successfully", logs));
    }
}
