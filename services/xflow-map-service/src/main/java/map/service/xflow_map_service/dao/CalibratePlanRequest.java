package map.service.xflow_map_service.dao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record  CalibratePlanRequest(
    @NotNull(message = "The control points are required for GPS calibration")
    @Size(min = 2, message = "At least 2 control points are required for GPS calibration")
    List<ControlPointRequest> controlPoints
) {

}
