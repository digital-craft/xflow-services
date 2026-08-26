package map.service.xflow_map_service.services;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import map.service.xflow_map_service.dao.MapVersionDAO.CreateMapVersionRequest;
import map.service.xflow_map_service.dao.MapVersionDAO.PublishMapVersionRequest;
import map.service.xflow_map_service.dao.MapVersionDAO.UpdateMapVersionRequest;
import map.service.xflow_map_service.dto.MapVersionResponse;
import map.service.xflow_map_service.models.MapVersion;
import map.service.xflow_map_service.models.enums.MapVersionStatus;
import map.service.xflow_map_service.models.enums.MapVersionUrgency;
import map.service.xflow_map_service.repositories.MapVersionRepository;
import map.service.xflow_map_service.utils.mappers.MapVersionMapper;

@Service
@RequiredArgsConstructor
public class MapVersionService {

    private final MapVersionRepository mapVersionRepository;
    private final MapVersionMapper mapVersionMapper;

    @Transactional
    public MapVersionResponse create(CreateMapVersionRequest request) {
        MapVersion mapVersion = mapVersionMapper.toEntity(request);
        
        Integer lastSequence = mapVersionRepository.findMaxSequenceNumberByTenantId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        mapVersion.setSequenceNumber(lastSequence + 1);

        mapVersion.setStatus(MapVersionStatus.DRAFT);
        if (mapVersion.getUrgency() == null) {
            mapVersion.setUrgency(MapVersionUrgency.NORMAL);
        }
        if (mapVersion.getAffectedLayers() == null) {
            mapVersion.setAffectedLayers(new String[0]);
        }

        MapVersion saved = mapVersionRepository.save(mapVersion);
        return mapVersionMapper.toResponse(saved);
    }

    public MapVersionResponse getById(UUID id) {
        MapVersion mapVersion = mapVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("map-version-not-found: " + id));
        return mapVersionMapper.toResponse(mapVersion);
    }

    public Page<MapVersionResponse> getAllByTenant(UUID tenantId, Pageable pageable) {
        return mapVersionRepository.findByTenantId(tenantId, pageable)
                .map(mapVersionMapper::toResponse);
    }

    @Transactional
    public MapVersionResponse update(UUID id, UpdateMapVersionRequest request) {
        MapVersion mapVersion = mapVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("map-version-not-found: " + id));

        if (!MapVersionStatus.DRAFT.equals(mapVersion.getStatus())) {
            throw new IllegalStateException("only-draft-versions-can-be-updated");
        }

        mapVersionMapper.updateEntityFromDto(request, mapVersion);
        MapVersion updated = mapVersionRepository.save(mapVersion);
        return mapVersionMapper.toResponse(updated);
    }

    @Transactional
    public MapVersionResponse publish(UUID id, PublishMapVersionRequest request) {
        MapVersion mapVersion = mapVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("map-version-not-found: " + id));

        if (!MapVersionStatus.DRAFT.equals(mapVersion.getStatus())) {
            throw new IllegalStateException("only-draft-versions-can-be-published");
        }

        mapVersion.setStatus(MapVersionStatus.PUBLISHED);
        mapVersion.setPublishedAt(OffsetDateTime.now());
        mapVersion.setPublishedBy(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        mapVersion.setCoexistenceUntil(request.coexistenceUntil());

        MapVersion published = mapVersionRepository.save(mapVersion);
        return mapVersionMapper.toResponse(published);
    }

    @Transactional
    public void delete(UUID id) {
        MapVersion mapVersion = mapVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("map-version-not-found: " + id));

        if (MapVersionStatus.PUBLISHED.equals(mapVersion.getStatus())) {
            throw new IllegalStateException("only-draft-versions-can-be-deleted");
        }
        mapVersionRepository.delete(mapVersion);
    }

}
