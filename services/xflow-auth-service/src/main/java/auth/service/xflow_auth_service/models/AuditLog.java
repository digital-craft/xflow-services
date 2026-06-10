package auth.service.xflow_auth_service.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", schema = "auth")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_source", nullable = false)
    private String serviceSource;

    @Column(name = "target_table", nullable = false)
    private String targetTable;

    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    public String getTargetInfo() {
        return this.targetTable + "::" + this.targetId;
    }
}