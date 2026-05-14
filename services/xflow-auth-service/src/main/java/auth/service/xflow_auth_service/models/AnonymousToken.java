package auth.service.xflow_auth_service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import auth.service.xflow_auth_service.models.enums.UserRole;
import auth.service.xflow_auth_service.models.bases.DateBaseModel;

import java.util.UUID;
import java.time.OffsetDateTime;

@Entity
@Table(name = "anonymous_token", schema = "auth")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AnonymousToken extends DateBaseModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

}
