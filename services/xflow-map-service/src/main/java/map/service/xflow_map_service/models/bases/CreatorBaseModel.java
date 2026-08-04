package map.service.xflow_map_service.models.bases;

import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @SuperBuilder
@NoArgsConstructor
public abstract class CreatorBaseModel {

    @CreatedBy
    @Column(name="created_by", nullable=false, updatable=false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name="updated_by", nullable=false)
    private UUID updatedBy;

}
