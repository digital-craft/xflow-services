package map.service.xflow_map_service.repositories;

import map.service.xflow_map_service.models.Obstacle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ObstacleRepository extends JpaRepository<Obstacle, UUID> {
    
    List<Obstacle> findByMapVersion_IdAndActiveTrue(UUID mapVersionId);

    void deleteByMapVersion_Id(UUID mapVersionId);
}
