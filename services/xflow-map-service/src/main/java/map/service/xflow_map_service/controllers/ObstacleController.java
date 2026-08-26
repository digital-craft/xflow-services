package map.service.xflow_map_service.controllers;

import jakarta.validation.Valid;
import map.service.xflow_map_service.dao.CreateObstacleRequest;
import map.service.xflow_map_service.dto.ObstacleResponse;
import map.service.xflow_map_service.services.ObstacleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/obstacles")
@RestController
public class ObstacleController {
    private final ObstacleService obstacleService;
    
    @PostMapping
    public ResponseEntity<ObstacleResponse> createObstacle(@Valid @RequestBody CreateObstacleRequest request) {
        return ResponseEntity.ok(obstacleService.createObstacle(request));
    }

    @GetMapping("/{id}/obstacles")
    public ResponseEntity<List<ObstacleResponse>> getObstaclesByVersion(@PathVariable UUID id) {
        return ResponseEntity.ok(obstacleService.getObstaclesByVersion(id));
    }
}
