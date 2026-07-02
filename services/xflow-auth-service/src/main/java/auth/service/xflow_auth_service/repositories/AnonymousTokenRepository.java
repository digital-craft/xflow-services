package auth.service.xflow_auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import auth.service.xflow_auth_service.models.AnonymousToken;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnonymousTokenRepository extends JpaRepository<AnonymousToken, UUID> {
    Optional<AnonymousToken> findById(UUID id);
}
