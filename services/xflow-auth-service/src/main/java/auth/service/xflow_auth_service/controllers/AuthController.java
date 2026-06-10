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
import auth.service.xflow_auth_service.dto.ApiResponse;
import auth.service.xflow_auth_service.dto.CreateOperatorResponse;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("logged-in", authService.login(request)));
    }

    @PostMapping("/login/operator")
    public ResponseEntity<ApiResponse<LoginResponse>> loginOperator(@Valid @RequestBody LoginPinRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("operator-logged-in", authService.loginOperator(request)));
    }

    @PostMapping("/login/anonymous")
    public ResponseEntity<ApiResponse<LoginResponse>> loginAnonymous(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String fingerprint = ip + "|" + (userAgent != null ? userAgent : "unknown");
        return ResponseEntity.ok(new ApiResponse<>("anonymous-logged-in", authService.loginAnonymous(fingerprint)));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(new ApiResponse<>("token-refreshed", authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiResponse<>("logged-out", null));
    }

    @PutMapping("/operator/{id}/regenerate-credentials")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CreateOperatorResponse>> regenerateOperatorCredentials(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
            "operator-credentials-regenerated",
            authService.regenerateOperatorCredentials(id))
        );
    } 

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CreateOperatorResponse>> changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
            "password-changed",
            authService.changePassword(userDetails.getUsername(), request))
        );
    }

    @PutMapping("/me/pin")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CreateOperatorResponse>> changePin(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePinRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
            "pin-changed",
            authService.changePin(userDetails.getUsername(), request))
        );
    }  
}