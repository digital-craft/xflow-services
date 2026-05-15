package auth.service.xflow_auth_service.models;

import jakarta.persistence.*;
import lombok.*;

import auth.service.xflow_auth_service.models.bases.DateBaseModel;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", schema = "auth")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken extends DateBaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    public boolean isExpired() {
        return expiryAt.isBefore(Instant.now());
    }
}