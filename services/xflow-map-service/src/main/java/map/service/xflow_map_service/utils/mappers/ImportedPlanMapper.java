package map.service.xflow_map_service.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import map.service.xflow_map_service.dto.ImportedPlanResponse;
import map.service.xflow_map_service.models.ImportedPlan;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) 
public interface ImportedPlanMapper {

    ImportedPlanResponse toResponse(ImportedPlan importedPlan);
}
