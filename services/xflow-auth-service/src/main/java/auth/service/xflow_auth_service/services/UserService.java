package auth.service.xflow_auth_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import auth.service.xflow_auth_service.repositories.UserRepository;
import auth.service.xflow_auth_service.dao.CreateOperatorRequest;
import auth.service.xflow_auth_service.dto.CreateOperatorResponse;
import auth.service.xflow_auth_service.models.User;
import auth.service.xflow_auth_service.models.enums.UserRole;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public CreateOperatorResponse createOperator(CreateOperatorRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("email-already-in-use");
        }
        String rawPassword = generateRandomPassword();
        String rawPin = generateRandomPin();
        User operator = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(rawPassword))
            .pin(passwordEncoder.encode(rawPin))
            .role(UserRole.ROLE_OPERATOR)
            .build();
        userRepository.save(operator);
        // sendCredentialsEmail(request.email(), rawPassword, rawPin);
        return new CreateOperatorResponse(
            operator.getEmail(),
            operator.getRole().name(),
            System.currentTimeMillis()
        );
    }

    @Transactional
    public CreateOperatorResponse regenerateOperatorCredentials(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("operator-not-found");
        }
        String rawPassword = generateRandomPassword();
        String rawPin = generateRandomPin();
        User operator = userRepository.findById(id).orElseThrow(() -> new RuntimeException("operator-not-found"));
        operator.setPassword(passwordEncoder.encode(rawPassword));
        operator.setPin(passwordEncoder.encode(rawPin));
        userRepository.save(operator);
        // sendCredentialsEmail(request.email(), rawPassword, rawPin);
        return new CreateOperatorResponse(
            operator.getEmail(),
            operator.getRole().name(),
            System.currentTimeMillis()
        );
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    private String generateRandomPin() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000;
        return String.valueOf(num);
    }

    private void sendCredentialsEmail(String email, String password, String pin) {
        System.out.println("----------------------------------------------------------------");
        System.out.println("SENDING EMAIL (MOCK) to : " + email);
        System.out.println("Your temporary password : " + password);
        System.out.println("Your quick login PIN : " + pin);
        System.out.println("----------------------------------------------------------------");
    }
}
