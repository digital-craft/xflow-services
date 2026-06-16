package auth.service.xflow_auth_service.auth;

import auth.service.xflow_auth_service.BaseIntegrationTest;
import auth.service.xflow_auth_service.services.AuditLogService;
import auth.service.xflow_auth_service.dao.AuditLogRequest;
import auth.service.xflow_auth_service.dto.AuditLogResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuditLogIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditLogService auditLogService;

    private AuditLogResponse createMockAuditLogResponse(String service, String table, String actor, String action) {
        return new AuditLogResponse(
            UUID.randomUUID(),
            service,
            table,
            UUID.randomUUID().toString(),
            actor,
            action,
            "Entity processed successfully",
            "127.0.0.1",
            OffsetDateTime.now()
        );
    }

    @Nested
    @DisplayName("Tests - Écriture des Logs d'Audit")
    class LogEventTests {

        @Test
        @DisplayName("POST /audit-logs - Sans Jeton (401)")
        void logEvent_NoToken_Returns401() throws Exception {
            AuditLogRequest request = new AuditLogRequest("auth-service", "users", "user-uuid-123", "CREATE", "User registered", "192.168.1.100");

            mockMvc.perform(post("/audit-logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "any_user@xflow.com")
        @DisplayName("POST /audit-logs - Utilisateur Connecté (Succès)")
        void logEvent_Authenticated_Success() throws Exception {
            AuditLogRequest request = new AuditLogRequest("auth-service", "users", "user-uuid-123", "CREATE", "User registered", "192.168.1.100");
            Mockito.doNothing().when(auditLogService).logEvent(any(AuditLogRequest.class));

            mockMvc.perform(post("/audit-logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("audit-log-saved-successfully"));
        }
    }

    @Nested
    @DisplayName("Tests - Lecture Paginée des Logs d'Audit")
    class GetAuditLogsTests {

        @Test
        @DisplayName("GET /audit-logs - Sans Jeton (401)")
        void getAuditLogs_NoToken_Returns401() throws Exception {
            mockMvc.perform(get("/audit-logs"))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "operator@xflow.com", roles = {"OPERATOR"})
        @DisplayName("GET /audit-logs - Rôle Opérateur Interdit (403)")
        void getAuditLogs_OperatorRole_Returns403() throws Exception {
            mockMvc.perform(get("/audit-logs"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("access-denied"));
        }

        @Test
        @WithMockUser(username = "admin@xflow.com", roles = {"ADMIN"})
        @DisplayName("GET /audit-logs - Rôle Admin & Filtres (Succès)")
        void getAuditLogs_AdminRole_Success() throws Exception {
            AuditLogResponse log1 = createMockAuditLogResponse("xflow-auth-service", "users", "admin", "UPDATE");
            AuditLogResponse log2 = createMockAuditLogResponse("xflow-auth-service", "users", "admin", "UPDATE");
            
            List<AuditLogResponse> list = List.of(log1, log2);
            Page<AuditLogResponse> pageResponse = new PageImpl<>(list, PageRequest.of(0, 20), list.size());

            Mockito.when(auditLogService.getAuditLogs(
                eq("xflow-auth-service"),
                eq("users"),
                eq("admin"),
                eq("UPDATE"),
                any(Pageable.class)
            )).thenReturn(pageResponse);

            mockMvc.perform(get("/audit-logs")
                        .param("serviceSource", "xflow-auth-service")
                    .param("targetTable", "users")
                    .param("actor", "admin")
                    .param("action", "UPDATE")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("audit-logs-retrieved-successfully"))
                .andExpect(jsonPath("$.results.content[0].serviceSource").value("xflow-auth-service"))
                .andExpect(jsonPath("$.results.content[1].action").value("UPDATE"))
                .andExpect(jsonPath("$.results.page.totalElements").value(2));
        }
    }
}