package map.service.xflow_map_service.utils.exceptions;

public class GraphNotConnectedException extends MapDomainException {
    public GraphNotConnectedException(String message) {
        super(message);
    }

    public GraphNotConnectedException() {
        super("Le graphe de circulation n'est pas connexe : certains nœuds ou zones sont isolés.");
    }
}