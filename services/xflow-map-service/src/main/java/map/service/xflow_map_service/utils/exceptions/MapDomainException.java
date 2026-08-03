package map.service.xflow_map_service.utils.exceptions;

public abstract class MapDomainException extends RuntimeException {
    protected MapDomainException(String message) {
        super(message);
    }
}