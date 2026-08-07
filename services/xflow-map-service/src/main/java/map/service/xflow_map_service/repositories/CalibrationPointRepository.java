package map.service.xflow_map_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import map.service.xflow_map_service.models.CalibrationPoint;

import java.util.UUID;

public interface CalibrationPointRepository extends JpaRepository<CalibrationPoint, UUID> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CalibrationPoint c WHERE c.planId.id = :planId")
    void deleteByPlanId(@Param("planId") UUID planId);
}