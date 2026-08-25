package map.service.xflow_map_service.controllers;

import jakarta.validation.Valid;
import map.service.xflow_map_service.dtos.CreateObstacleRequest;
import map.service.xflow_map_service.dtos.ObstacleResponse;
import map.service.xflow_map_service.services.ObstacleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/obstacles")
public class ObstacleController {
    private final ObstacleService obstacleService;
    
    @PostMapping
    public ResponseEntity<ObstacleResponse> createObstacle(@Valid @RequestBody CreateObstacleRequest request) {
        ObstacleResponse response = obstacleService.createObstacle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/obstacles")
    public ResponseEntity<List<ObstacleResponse>> getObstaclesByVersion(@PathVariable UUID id) {
        return ResponseEntity.ok(obstacleService.getObstaclesByVersion(id));
    }
}
