package auth.service.xflow_auth_service.utils.mappers;

import auth.service.xflow_auth_service.dto.UserResponse;
import auth.service.xflow_auth_service.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) 
public interface UserMapper {

    UserResponse toResponse(User user);
}