package map.service.xflow_map_service.utils.mappers;

import map.service.xflow_map_service.dto.ObstacleResponse;
import map.service.xflow_map_service.models.Obstacle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = { GeoJsonLineStringMapper.class }
)
public interface ObstacleMapper {

    @Mapping(target = "mapVersionId", source = "mapVersion.id")
    @Mapping(target = "geometry", source = "geom")
    ObstacleResponse toResponse(Obstacle obstacle);
}