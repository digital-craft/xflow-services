package map.service.xflow_map_service.utils.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.stereotype.Component;

@Component
public class GeoJsonLineStringMapper {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LineString toLineString(GeoJsonObject geoJsonObject) {
        if (geoJsonObject == null) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(geoJsonObject);
            GeoJsonReader reader = new GeoJsonReader(geometryFactory);
            Geometry geometry = reader.read(json);

            if (geometry instanceof LineString lineString) {
                return lineString;
            }
            throw new IllegalArgumentException("Provided GeoJSON geometry must be a LineString.");
        } catch (Exception e) {
            throw new RuntimeException("Error converting GeoJSON to JTS LineString", e);
        }
    }

    public GeoJsonObject toGeoJson(LineString lineString) {
        if (lineString == null) {
            return null;
        }
        try {
            GeoJsonWriter writer = new GeoJsonWriter();
            String json = writer.write(lineString);
            return objectMapper.readValue(json, GeoJsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JTS LineString to GeoJSON", e);
        }
    }
}