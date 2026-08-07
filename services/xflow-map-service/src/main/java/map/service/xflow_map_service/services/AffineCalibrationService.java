package map.service.xflow_map_service.services;

import org.springframework.stereotype.Component;

import map.service.xflow_map_service.dao.ControlPointRequest;

import java.util.ArrayList;
import java.util.List;

@Component
public class AffineCalibrationService {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    public record CalibrationResult(
            List<Double> residualErrors,
            double meanErrorMeters
    ) {}

    public CalibrationResult calculateResiduals(List<ControlPointRequest> points) {
        int n = points.size();

        if (n <= 2) {
            List<Double> zeroErrors = points.stream().map(p -> 0.0).toList();
            return new CalibrationResult(zeroErrors, 0.0);
        }

        double sumX = 0, sumY = 0, sumLon = 0, sumLat = 0;
        for (ControlPointRequest p : points) {
            sumX += p.pixelX();
            sumY += p.pixelY();
            sumLon += p.longitude();
            sumLat += p.latitude();
        }

        double meanX = sumX / n;
        double meanY = sumY / n;
        double meanLon = sumLon / n;
        double meanLat = sumLat / n;

        double numA = 0, denA = 0;
        for (ControlPointRequest p : points) {
            double dx = p.pixelX() - meanX;
            double dy = p.pixelY() - meanY;
            double dLon = p.longitude() - meanLon;
            double dLat = p.latitude() - meanLat;

            numA += (dx * dLon + dy * dLat);
            denA += (dx * dx + dy * dy);
        }

        double scale = (denA != 0) ? numA / denA : 0;

        List<Double> errors = new ArrayList<>();
        double totalErrorSum = 0.0;

        for (ControlPointRequest p : points) {
            double estimatedLon = meanLon + (p.pixelX() - meanX) * scale;
            double estimatedLat = meanLat + (p.pixelY() - meanY) * scale;

            double errorMeters = haversineDistance(p.latitude(), p.longitude(), estimatedLat, estimatedLon);
            errors.add(errorMeters);
            totalErrorSum += errorMeters;
        }

        double meanError = totalErrorSum / n;
        return new CalibrationResult(errors, meanError);
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}