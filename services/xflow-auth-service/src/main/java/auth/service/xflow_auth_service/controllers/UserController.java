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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import auth.service.xflow_auth_service.services.UserService;
import auth.service.xflow_auth_service.dao.CreateOperatorRequest;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.dto.XflowResponse;
import auth.service.xflow_auth_service.models.enums.UserRole;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User and operator management endpoints (Admin only)")
public class UserController {
    private final UserService userService;

    @PostMapping("/operator")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Create a new operator user",
        description = "Creates a new operator user account with auto-generated credentials (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operator created successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<UserResponse>> createOperator(
        @Valid @RequestBody CreateOperatorRequest request
    ) {
        return ResponseEntity.ok(new XflowResponse<>("operator-created", userService.createOperator(request)));
    }

    @PutMapping("/operator/{id}/toggle-active-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Toggle operator active status",
        description = "Activates or deactivates an operator user account (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active status toggled successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Operator not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @Parameter(name = "id", description = "Operator user ID", required = true)
    public ResponseEntity<XflowResponse<UserResponse>> toggleOperatorActiveStatus(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(new XflowResponse<>(
            "operator-active-status-toggled",
            userService.toggleOperatorActiveStatus(id))
        );
    }

    @GetMapping("/operators")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Get all operators (paginated)",
        description = "Retrieves a paginated list of all operators with optional filtering (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operators retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<XflowResponse<Page<UserResponse>>> getAllUsers(
        @Parameter(description = "Filter by email address") @RequestParam(required = false) String email,
        @Parameter(description = "Filter by user role") @RequestParam(required = false) UserRole role,
        @Parameter(description = "Filter by active status") @RequestParam(required = false) boolean active,
        @PageableDefault(page = 0, size = 20, sort = "email", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<UserResponse> usersPage = userService.getAllUsersPaginated(email, role, active, pageable);
        return ResponseEntity.ok(new XflowResponse<>("users-retrieved-successfully", usersPage));
    }
}
