package auth.service.xflow_auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import auth.service.xflow_auth_service.models.AnonymousToken;

import java.util.Optional;
import java.util.UUID;

public interface AnonymousTokenRepository extends JpaRepository<AnonymousToken, UUID> {
    Optional<AnonymousToken> findById(UUID id);
}
