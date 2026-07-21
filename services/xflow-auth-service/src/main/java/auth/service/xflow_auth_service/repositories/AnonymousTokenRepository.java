package auth.service.xflow_auth_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import auth.service.xflow_auth_service.models.AnonymousToken;

import java.util.Optional;
import java.util.UUID;

public interface AnonymousTokenRepository extends JpaRepository<AnonymousToken, UUID> {
    @NonNull
    Optional<AnonymousToken> findById(UUID id);
}
