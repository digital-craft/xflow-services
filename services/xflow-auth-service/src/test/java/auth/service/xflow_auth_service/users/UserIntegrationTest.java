package auth.service.xflow_auth_service.users;

import auth.service.xflow_auth_service.BaseIntegrationTest;
import auth.service.xflow_auth_service.services.UserService;
import auth.service.xflow_auth_service.dao.OperatorRequest;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.models.enums.UserRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse createMockUserResponse(UUID id, String email, UserRole role, boolean active) {
        return new UserResponse(
            id,
            email,
            role,
            active,
            false,
            true,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            1
        );
    }

    @Nested
    @DisplayName("Tests - Création Opérateur")
    class CreateOperatorTests {

        @Test
        @DisplayName("POST /users/operator - Sans Jeton (401)")
        void createOperator_NoToken_Returns401() throws Exception {
            OperatorRequest request = new OperatorRequest("op1@xflow.com");

            mockMvc.perform(post("/users/operator")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "operator@xflow.com", roles = {"OPERATOR"})
        @DisplayName("POST /users/operator - Rôle Opérateur Interdit (403)")
        void createOperator_OperatorRole_Returns403() throws Exception {
            OperatorRequest request = new OperatorRequest("op1@xflow.com");

            mockMvc.perform(post("/users/operator")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("access-denied"));
        }

        @Test
        @WithMockUser(username = "admin@xflow.com", roles = {"ADMIN"})
        @DisplayName("POST /users/operator - Rôle Admin (Succès)")
        void createOperator_AdminRole_Success() throws Exception {
            OperatorRequest request = new OperatorRequest("op1@xflow.com");
            UserResponse response = createMockUserResponse(UUID.randomUUID(), "op1@xflow.com", UserRole.ROLE_OPERATOR, true);
            
            Mockito.when(userService.createOperator(any(OperatorRequest.class))).thenReturn(response);

            mockMvc.perform(post("/users/operator")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("operator-created"))
                .andExpect(jsonPath("$.results.email").value("op1@xflow.com"))
                .andExpect(jsonPath("$.results.role").value("ROLE_OPERATOR"));
        }
    }

    @Nested
    @DisplayName("Tests - Bascule du Statut Actif")
    class ToggleActiveStatusTests {

        @Test
        @DisplayName("PUT /users/operator/{id}/toggle-active-status - Sans Jeton (401)")
        void toggleStatus_NoToken_Returns401() throws Exception {
            UUID targetId = UUID.randomUUID();

            mockMvc.perform(put("/users/operator/" + targetId + "/toggle-active-status"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "operator@xflow.com", roles = {"OPERATOR"})
        @DisplayName("PUT /users/operator/{id}/toggle-active-status - Rôle Opérateur Interdit (403)")
        void toggleStatus_OperatorRole_Returns403() throws Exception {
            UUID targetId = UUID.randomUUID();

            mockMvc.perform(put("/users/operator/" + targetId + "/toggle-active-status"))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin@xflow.com", roles = {"ADMIN"})
        @DisplayName("PUT /users/operator/{id}/toggle-active-status - Rôle Admin (Succès)")
        void toggleStatus_AdminRole_Success() throws Exception {
            UUID targetId = UUID.randomUUID();
            UserResponse response = createMockUserResponse(targetId, "op1@xflow.com", UserRole.ROLE_OPERATOR, false);
            
            Mockito.when(userService.toggleOperatorActiveStatus(eq(targetId))).thenReturn(response);

            mockMvc.perform(put("/users/operator/" + targetId + "/toggle-active-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("operator-active-status-toggled"))
                .andExpect(jsonPath("$.results.active").value(false));
        }
    }

    @Nested
    @DisplayName("Tests - Récupération Paginée des Utilisateurs")
    class GetOperatorsPaginatedTests {

        @Test
        @DisplayName("GET /users/operators - Sans Jeton (401)")
        void getAllUsers_NoToken_Returns401() throws Exception {
            mockMvc.perform(get("/users/operators"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "operator@xflow.com", roles = {"OPERATOR"})
        @DisplayName("GET /users/operators - Rôle Opérateur Interdit (403)")
        void getAllUsers_OperatorRole_Returns403() throws Exception {
            mockMvc.perform(get("/users/operators"))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin@xflow.com", roles = {"ADMIN"})
        @DisplayName("GET /users/operators - Rôle Admin & Filtres (Succès)")
        void getAllUsers_AdminRole_Success() throws Exception {
            UserResponse user1 = createMockUserResponse(UUID.randomUUID(), "op1@xflow.com", UserRole.ROLE_OPERATOR, true);
            UserResponse user2 = createMockUserResponse(UUID.randomUUID(), "op2@xflow.com", UserRole.ROLE_OPERATOR, true);
            
            List<UserResponse> list = List.of(user1, user2);
            Page<UserResponse> pageResponse = new PageImpl<>(list, PageRequest.of(0, 20), list.size());

            Mockito.when(userService.getAllUsersPaginated(
                eq("op"), 
                eq(UserRole.ROLE_OPERATOR), 
                eq(true), 
                any(Pageable.class)
            )).thenReturn(pageResponse);

            mockMvc.perform(get("/users/operators")
                    .param("email", "op")
                    .param("role", "ROLE_OPERATOR")
                    .param("active", "true")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("users-retrieved-successfully"))
                .andExpect(jsonPath("$.results.content[0].email").value("op1@xflow.com"))
                .andExpect(jsonPath("$.results.content[1].email").value("op2@xflow.com"))
                .andExpect(jsonPath("$.results.page.totalElements").value(2));
        }
    }
}