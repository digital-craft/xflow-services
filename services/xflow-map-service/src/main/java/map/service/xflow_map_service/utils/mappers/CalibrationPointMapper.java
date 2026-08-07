package map.service.xflow_map_service.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import map.service.xflow_map_service.dto.CalibrationPointResponse;
import map.service.xflow_map_service.models.CalibrationPoint;
import map.service.xflow_map_service.models.ImportedPlan;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING) 
public interface CalibrationPointMapper {
    
    @Mapping(target = "planId", source = "planId")
    CalibrationPointResponse toResponse(CalibrationPoint calibrationPoint);

    default UUID map(ImportedPlan importedPlan) {
        return importedPlan == null ? null : importedPlan.getId();
    }
}
