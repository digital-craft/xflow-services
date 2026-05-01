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
@Table(name = "users", schema = "auth")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User extends DateBaseModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;
    
}