package auth.service.xflow_auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.UserService;
import auth.service.xflow_auth_service.dao.CreateOperatorRequest;
import auth.service.xflow_auth_service.dto.CreateOperatorResponse;
import auth.service.xflow_auth_service.dto.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/operator")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CreateOperatorResponse>> createOperator(
        @Valid @RequestBody CreateOperatorRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>("operator-created", userService.createOperator(request)));
    }

    @GetMapping("/operator/{id}/regenerate-credentials")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CreateOperatorResponse>> regenerateOperatorCredentials(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
            "operator-credentials-regenerated",
            userService.regenerateOperatorCredentials(id))
        );
    }   
}
