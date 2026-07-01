package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.dao.OperatorRequest;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.enums.UserRole;
import auth.service.xflow_auth_service.utils.events.AuditEvent;
import auth.service.xflow_auth_service.utils.security.SecurityUtils;
import auth.service.xflow_auth_service.utils.mappers.UserMapper;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.utils.events.RequestContextHolder;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    
    @Transactional
    public UserResponse createOperator(OperatorRequest request) {
        String ip = RequestContextHolder.getClientIp();
        String actorEmail = RequestContextHolder.getUserEmail();
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("email-already-in-use");
        }
        String rawPassword = securityUtils.generateRandomPassword();
        String rawPin = securityUtils.generateRandomPin();
        User operator = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(rawPassword))
            .pin(passwordEncoder.encode(rawPin))
            .role(UserRole.ROLE_OPERATOR)
            .build();
        Map<String, String> emailFields = Map.of(
            "login", request.email(),
            "password", rawPassword,
            "code", rawPin,
            "role", UserRole.ROLE_OPERATOR.name(),
            "primary_button", "https://www.google.com",
            "privacy", "https://www.google.com",
            "terms", "https://www.google.com"
        );
        userRepository.save(operator);
        emailService.sendTemplateEmailSync(
            null,
            request.email(),
            "Welcome to XFlow — Your carrier access",
            "classpath:templates/account-created.html",
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            operator.getId().toString(),
            actorEmail != null ? actorEmail : "unknown",
            "USER_CREATE_OPERATOR",
            "Operator created successfully",
            ip
        ));
        return userMapper.toResponse(operator);
    }

    @Transactional
    public UserResponse toggleOperatorActiveStatus(UUID id) {
        String ip = RequestContextHolder.getClientIp();
        String actorEmail = RequestContextHolder.getUserEmail();
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("operator-not-found");
        }
        User operator = userRepository.findById(id).orElseThrow(() -> new RuntimeException("operator-not-found"));
        if (operator.getRole() != UserRole.ROLE_OPERATOR) {
            throw new RuntimeException("only-operator-accounts-can-be-toggled");
        }
        Map<String, String> emailFields;
        String subject;
        String template;
        if (operator.isActive()) {
            subject = "Your account has been deactivated";
            template = "classpath:templates/account-disabled.html";
            emailFields = Map.of(
                "login", operator.getEmail(),
                "admin", actorEmail,
                "reason", "Fraudulent activity",
                "primary_button", "https://www.google.com",
                "privacy", "https://www.google.com",
                "terms", "https://www.google.com"
            );
        } else {
            subject = "Your account has been reactivated";
            template = "classpath:templates/account-enabled.html";
            emailFields = Map.of(
                "login", operator.getEmail(),
                "admin", actorEmail,
                "role", operator.getRole().name(),
                "primary_button", "https://www.google.com",
                "privacy", "https://www.google.com",
                "terms", "https://www.google.com"
            );
        }
        operator.setActive(!operator.isActive()); 
        userRepository.save(operator);
        emailService.sendTemplateEmailSync(
            null,
            operator.getEmail(),
            subject,
            template,
            emailFields
        );
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            operator.getId().toString(),
            actorEmail != null ? actorEmail : "unknown",
            operator.isActive() ? "USER_ACTIVATE_OPERATOR" : "USER_DEACTIVATE_OPERATOR",
            operator.isActive() ? "Operator activated successfully" : "Operator deactivated successfully",
            ip
        ));
        return userMapper.toResponse(operator);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsersPaginated(String email, UserRole role, Boolean active, Pageable pageable) {
        String ip = RequestContextHolder.getClientIp();
        String actorEmail = RequestContextHolder.getUserEmail();
        Page<User> users = userRepository.findWithFilters(email, role, active, pageable);
        eventPublisher.publishEvent(new AuditEvent(
            "xflow-auth-service",
            "users",
            "unknown",
            actorEmail != null ? actorEmail : "unknown",
            "USER_LIST",
            "Listed users with filters - page " + pageable.getPageNumber(),
            ip
        ));
        return users.map(userMapper::toResponse);
    }
}
