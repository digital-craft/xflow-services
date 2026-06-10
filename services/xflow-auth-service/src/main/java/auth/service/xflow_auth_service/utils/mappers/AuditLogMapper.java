package auth.service.xflow_auth_service.utils.mappers;

import auth.service.xflow_auth_service.dto.AuditLogResponse;
import auth.service.xflow_auth_service.models.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) 
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
