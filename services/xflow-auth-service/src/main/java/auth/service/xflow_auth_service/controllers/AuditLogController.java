package auth.service.xflow_auth_service.controllers;

import org.springframework.http.ResponseEntity;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.AuditLogService;
import auth.service.xflow_auth_service.dao.AuditLogRequest;
import auth.service.xflow_auth_service.dto.XflowResponse;
import auth.service.xflow_auth_service.dto.AuditLogResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit logging endpoints for tracking system events and changes")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Log an audit event",
        description = "Creates a new audit log entry for system events and changes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit log saved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<Object>> logEvent(@RequestBody AuditLogRequest request) {
        auditLogService.logEvent(request);
        return ResponseEntity.ok(new XflowResponse<>("audit-log-saved-successfully", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Retrieve audit logs",
        description = "Retrieves paginated audit logs with optional filtering by service, table, actor, or action (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<Page<AuditLogResponse>>> getAuditLogs(
            @Parameter(description = "Filter by source service") @RequestParam(required = false) String serviceSource,
            @Parameter(description = "Filter by target table") @RequestParam(required = false) String targetTable,
            @Parameter(description = "Filter by actor (user who performed action)") @RequestParam(required = false) String actor,
            @Parameter(description = "Filter by action type (CREATE, UPDATE, DELETE, etc.)") @RequestParam(required = false) String action,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AuditLogResponse> logs = auditLogService.getAuditLogs(
            serviceSource, targetTable, actor, action, pageable
        );
        return ResponseEntity.ok(new XflowResponse<>("audit-logs-retrieved-successfully", logs));
    }
}
