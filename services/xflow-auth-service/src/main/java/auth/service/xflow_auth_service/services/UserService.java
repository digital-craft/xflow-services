package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.dao.CreateOperatorRequest;
import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.enums.UserRole;
import auth.service.xflow_auth_service.utils.security.SecurityUtils;
import auth.service.xflow_auth_service.utils.mappers.UserMapper;
import auth.service.xflow_auth_service.dto.UserResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    
    @Transactional
    public UserResponse createOperator(CreateOperatorRequest request) {
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
        userRepository.save(operator);
        securityUtils.sendCredentialsEmail(operator.getEmail(), rawPassword, rawPin);
        return userMapper.toResponse(operator);
    }

    @Transactional
    public UserResponse toggleOperatorActiveStatus(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("operator-not-found");
        }
        User operator = userRepository.findById(id).orElseThrow(() -> new RuntimeException("operator-not-found"));
        if (operator.getRole() != UserRole.ROLE_OPERATOR) {
            throw new RuntimeException("only-operator-accounts-can-be-toggled");
        }
        operator.setActive(!operator.isActive());
        userRepository.save(operator);
        return userMapper.toResponse(operator);
    }

    @Transactional
    public Page<UserResponse> getAllUsersPaginated(String email, UserRole role, Boolean active, Pageable pageable) {
        Page<User> users = userRepository.findWithFilters(email, role, active, pageable);
        return users.map(userMapper::toResponse);
    }
}
