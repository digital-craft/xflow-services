package map.service.xflow_map_service.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;

import map.service.xflow_map_service.dao.MapVersionDAO.*;
import map.service.xflow_map_service.dto.MapVersionResponse;
import map.service.xflow_map_service.models.MapVersion;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MapVersionMapper {

    MapVersion toEntity(CreateMapVersionRequest request);

    MapVersionResponse toResponse(MapVersion mapVersion);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateMapVersionRequest request, @MappingTarget MapVersion entity);
}