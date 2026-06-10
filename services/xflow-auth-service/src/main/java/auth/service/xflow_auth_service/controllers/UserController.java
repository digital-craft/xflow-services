package auth.service.xflow_auth_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.UserService;
import auth.service.xflow_auth_service.dao.CreateOperatorRequest;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.dto.ApiResponse;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.models.enums.UserRole;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/operator")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createOperator(
        @Valid @RequestBody CreateOperatorRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>("operator-created", userService.createOperator(request)));
    }

    @PutMapping("/operator/{id}/toggle-active-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleOperatorActiveStatus(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
            "operator-active-status-toggled",
            userService.toggleOperatorActiveStatus(id))
        );
    }

    @GetMapping("/operators")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) UserRole role,
        @RequestParam(required = false) boolean active,
        @PageableDefault(page = 0, size = 10, sort = "email", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<UserResponse> usersPage = userService.getAllUsersPaginated(email, role, active, pageable);
        return ResponseEntity.ok(new ApiResponse<>("users-retrieved-successfully", usersPage));
    }
}
