package auth.service.xflow_auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.AuthService;
import auth.service.xflow_auth_service.dao.LoginRequest;
import auth.service.xflow_auth_service.dao.LogoutRequest;
import auth.service.xflow_auth_service.dao.LoginPinRequest;
import auth.service.xflow_auth_service.dao.RefreshTokenRequest;
import auth.service.xflow_auth_service.dao.ChangePasswordRequest;
import auth.service.xflow_auth_service.dao.ChangePinRequest;
import auth.service.xflow_auth_service.dto.LoginResponse;
import auth.service.xflow_auth_service.dto.XflowResponse;
import auth.service.xflow_auth_service.dto.UserResponse;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints for user and operator login/logout")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password credentials"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful", 
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<XflowResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new XflowResponse<>("logged-in", authService.login(request)));
    }

    @PostMapping("/login/operator")
    @Operation(
        summary = "Operator login with PIN",
        description = "Authenticates an operator user with a 6-digit PIN"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operator login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid PIN"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<XflowResponse<LoginResponse>> loginOperator(@Valid @RequestBody LoginPinRequest request) {
        return ResponseEntity.ok(new XflowResponse<>("operator-logged-in", authService.loginOperator(request)));
    }

    @PostMapping("/login/anonymous")
    @Operation(
        summary = "Anonymous user login",
        description = "Authenticates an anonymous user based on IP and User-Agent fingerprint"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Anonymous login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "429", description = "Too many login attempts")
    })
    public ResponseEntity<XflowResponse<LoginResponse>> loginAnonymous(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String fingerprint = ip + "|" + (userAgent != null ? userAgent : "unknown");
        return ResponseEntity.ok(new XflowResponse<>("anonymous-logged-in", authService.loginAnonymous(fingerprint)));
    }
    
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Generates a new access token using a valid refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<XflowResponse<Object>> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(new XflowResponse<>("token-refreshed", authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logs out a user and invalidates their tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<XflowResponse<Object>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new XflowResponse<>("logged-out", null));
    }

    @PutMapping("/operator/{id}/regenerate-credentials")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Regenerate operator credentials",
        description = "Regenerates password and PIN for an operator user (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credentials regenerated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Operator not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<UserResponse>> regenerateOperatorCredentials(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(new XflowResponse<>(
            "operator-credentials-regenerated",
            authService.regenerateOperatorCredentials(id))
        );
    } 

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Change user password",
        description = "Allows an authenticated user to change their password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<UserResponse>> changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(new XflowResponse<>(
            "password-changed",
            authService.changePassword(userDetails.getUsername(), request))
        );
    }

    @PutMapping("/me/pin")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Change user PIN",
        description = "Allows an authenticated user to change their PIN"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PIN changed successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<UserResponse>> changePin(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePinRequest request
    ) {
        return ResponseEntity.ok(new XflowResponse<>(
            "pin-changed",
            authService.changePin(userDetails.getUsername(), request))
        );
    }  
}