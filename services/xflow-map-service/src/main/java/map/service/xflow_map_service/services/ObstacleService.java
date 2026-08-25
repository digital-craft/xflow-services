package map.service.xflow_map_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import map.service.xflow_map_service.dao.CreateObstacleRequest;
import map.service.xflow_map_service.dto.ObstacleResponse;
import map.service.xflow_map_service.utils.mappers.GeoJsonLineStringMapper;
import map.service.xflow_map_service.utils.mappers.ObstacleMapper;
import map.service.xflow_map_service.models.MapVersion;
import map.service.xflow_map_service.models.Obstacle;
import map.service.xflow_map_service.models.enums.MapVersionStatus;
import map.service.xflow_map_service.repositories.MapVersionRepository;
import map.service.xflow_map_service.repositories.ObstacleRepository;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObstacleService {
    private final ObstacleRepository obstacleRepository;
    private final MapVersionRepository mapVersionRepository;
    private final GeoJsonLineStringMapper lineStringMapper;
    private final ObstacleMapper obstacleMapper;

    @Transactional
    public ObstacleResponse createObstacle(CreateObstacleRequest request) {
        MapVersion mapVersion = mapVersionRepository.findById(request.mapVersionId())
                .orElseThrow(() -> new IllegalArgumentException("Map version not found with ID: " + request.mapVersionId()));

        if (mapVersion.getStatus() != MapVersionStatus.DRAFT) {
            throw new IllegalStateException("Cannot add obstacles to a non-draft map version: " + request.mapVersionId());
        }

        LineString lineString = lineStringMapper.toLineString(request.geometry());

        Obstacle obstacle = new Obstacle();
        obstacle.setMapVersion(mapVersion);
        obstacle.setGeom(lineString);
        obstacle.setActive(true);

        Obstacle saved = obstacleRepository.save(obstacle);
        return obstacleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ObstacleResponse> getObstaclesByVersion(UUID mapVersionId) {
        return obstacleRepository.findByMapVersion_IdAndActiveTrue(mapVersionId)
                .stream()
                .map(obstacleMapper::toResponse)
                .toList();
    }
}
