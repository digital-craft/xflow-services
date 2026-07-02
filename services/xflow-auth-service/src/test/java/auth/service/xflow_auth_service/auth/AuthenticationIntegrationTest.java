package auth.service.xflow_auth_service.auth;

import auth.service.xflow_auth_service.BaseIntegrationTest;
import auth.service.xflow_auth_service.services.AuthService;
import auth.service.xflow_auth_service.dao.*;
import auth.service.xflow_auth_service.dto.*;
import auth.service.xflow_auth_service.models.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Nested
    @DisplayName("Tests - Authentification Publique")
    class PublicAuthenticationTests {

        @Test
        @DisplayName("POST /login - Succès")
        void login_Success() throws Exception {
            LoginRequest request = new LoginRequest("user@xflow.com", "password123");
            LoginResponse response = new LoginResponse("mock-access-token", "mock-refresh-token", "Bearer", "read", 3600L);
            Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("logged-in"))
                .andExpect(jsonPath("$.results.token").value("mock-access-token"));
        }

        @Test
        @DisplayName("POST /login - Identifiants Invalides (401 via ExceptionHandler)")
        void login_BadCredentials() throws Exception {
            LoginRequest request = new LoginRequest("user@xflow.com", "wrong-password");
            Mockito.when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("invalid-credentials"));

            mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("invalid-credentials"));
        }

        @Test
        @DisplayName("POST /login/operator - Succès")
        void loginOperator_Success() throws Exception {
            LoginPinRequest request = new LoginPinRequest("123456");
            LoginResponse response = new LoginResponse("operator-access-token", "operator-refresh-token", "Bearer", "read", 3600L);
            Mockito.when(authService.loginOperator(any(LoginPinRequest.class))).thenReturn(response);

            mockMvc.perform(post("/login/operator")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("operator-logged-in"));
        }

        @Test
        @DisplayName("POST /login/anonymous - Succès avec Fingerprint")
        void loginAnonymous_Success() throws Exception {
            LoginResponse response = new LoginResponse("anon-access-token", "anon-refresh-token", "Bearer", "read", 3600L);
            Mockito.when(authService.loginAnonymous(any(String.class))).thenReturn(response);

            mockMvc.perform(post("/login/anonymous")
                    .remoteAddress("192.168.1.50")
                    .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("anonymous-logged-in"));
        }

        @Test
        @DisplayName("POST /refresh - Succès")
        void refreshToken_Success() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
            LoginResponse response = new LoginResponse("new-access-token", "valid-refresh-token", "Bearer", "read", 3600L);
            Mockito.when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

            mockMvc.perform(post("/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("token-refreshed"));
        }

        @Test
        @DisplayName("POST /logout - Succès")
        void logout_Success() throws Exception {
            LogoutRequest request = new LogoutRequest("valid-refresh-token");

            mockMvc.perform(post("/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("logged-out"));
        }
    }

    @Nested
    @DisplayName("Tests - Validation de la Sécurité et Droits")
    class SecuredAuthenticationTests {
        private UserResponse createMockUserResponse(UUID id, String email, UserRole role) {
            return new UserResponse(
                id, 
                email, 
                role, 
                true, 
                false, 
                true, 
                OffsetDateTime.now(), 
                OffsetDateTime.now(), 
                1
            );
        }

        @Test
        @DisplayName("GET /me - Absent de Jeton (401 intercepté par GlobalExceptionHandler)")
        void getCurrentUserInfo_MissingToken_Returns401() throws Exception {
            mockMvc.perform(get("/me")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("missing-or-invalid-token"))
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @WithMockUser(username = "igor@xflow.com")
        @DisplayName("GET /me - Connecté (Succès)")
        void getCurrentUserInfo_Authenticated_Success() throws Exception {
            UserResponse userResponse = createMockUserResponse(UUID.randomUUID(), "igor@xflow.com", UserRole.ROLE_PARTICIPANT);
            Mockito.when(authService.getCurrentUserInfo("igor@xflow.com")).thenReturn(userResponse);

            mockMvc.perform(get("/me")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("current-user-info"))
                .andExpect(jsonPath("$.results.email").value("igor@xflow.com"));
        }

        @Test
        @DisplayName("PUT /me/password - Absent de Jeton (401)")
        void changePassword_MissingToken_Returns401() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass@123", "newPass@123");

            mockMvc.perform(put("/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "igor@xflow.com")
        @DisplayName("PUT /me/password - Connecté (Succès)")
        void changePassword_Authenticated_Success() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass@123", "newPass@123");
            UserResponse userResponse = createMockUserResponse(UUID.randomUUID(), "igor@xflow.com", UserRole.ROLE_PARTICIPANT);
            Mockito.when(authService.changePassword(eq("igor@xflow.com"), any(ChangePasswordRequest.class))).thenReturn(userResponse);

            mockMvc.perform(put("/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("password-changed"));
        }

        @Test
        @DisplayName("PUT /me/pin - Absent de Jeton (401)")
        void changePin_MissingToken_Returns401() throws Exception {
            ChangePinRequest request = new ChangePinRequest("111111", "222222");

            mockMvc.perform(put("/me/pin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "igor@xflow.com")
        @DisplayName("PUT /me/pin - Connecté (Succès)")
        void changePin_Authenticated_Success() throws Exception {
            ChangePinRequest request = new ChangePinRequest("111111", "222222");
            UserResponse userResponse = createMockUserResponse(UUID.randomUUID(), "igor@xflow.com", UserRole.ROLE_PARTICIPANT);
            Mockito.when(authService.changePin(eq("igor@xflow.com"), any(ChangePinRequest.class))).thenReturn(userResponse);

            mockMvc.perform(put("/me/pin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pin-changed"));
        }

        @Test
        @WithMockUser(username = "operator@xflow.com", roles = {"OPERATOR"})
        @DisplayName("PUT /operator/{id}/regenerate-credentials - Rôle Insuffisant (403 via ExceptionHandler)")
        void regenerateCredentials_InsufficientRole_Returns403() throws Exception {
            UUID targetId = UUID.randomUUID();

            mockMvc.perform(put("/operator/" + targetId + "/regenerate-credentials")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("access-denied"))
                .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @WithMockUser(username = "admin@xflow.com", roles = {"ADMIN"})
        @DisplayName("PUT /operator/{id}/regenerate-credentials - Rôle Admin (Succès)")
        void regenerateCredentials_AdminRole_Success() throws Exception {
            UUID targetId = UUID.randomUUID();
            UserResponse userResponse = createMockUserResponse(targetId, "operator@xflow.com", UserRole.ROLE_OPERATOR);
            Mockito.when(authService.regenerateOperatorCredentials(eq(targetId))).thenReturn(userResponse);

            mockMvc.perform(put("/operator/" + targetId + "/regenerate-credentials")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("operator-credentials-regenerated"));
        }
    }
}