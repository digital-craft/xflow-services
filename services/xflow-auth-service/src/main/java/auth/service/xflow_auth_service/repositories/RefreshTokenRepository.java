package auth.service.xflow_auth_service.repositories;

import auth.service.xflow_auth_service.models.RefreshToken;
import auth.service.xflow_auth_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);

    void deleteByUserId(UUID userId);
}