package map.service.xflow_map_service.models.bases;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @SuperBuilder
@NoArgsConstructor
public abstract class DateBaseModel extends CreatorBaseModel {

    @CreatedDate
    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;
}