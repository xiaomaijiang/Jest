package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BOTTOM_RIGHT;
import static io.searchbox.core.search.aggregation.AggregationField.BOUNDS;
import static io.searchbox.core.search.aggregation.AggregationField.LAT;
import static io.searchbox.core.search.aggregation.AggregationField.LON;
import static io.searchbox.core.search.aggregation.AggregationField.TOP_LEFT;

/**
 * @author cfstout
 */
public class GeoBoundsAggregation extends MetricAggregation {

    public static final String TYPE = "geo_bounds";

    private Double topLeftLat;
    private Double topLeftLon;
    private Double bottomRightLat;
    private Double bottomRightLon;

    public GeoBoundsAggregation(String name, JsonNode geoBoundsAggregation) {
        super(name, geoBoundsAggregation);
        if (geoBoundsAggregation.has(String.valueOf(BOUNDS))) {
            JsonNode bounds = geoBoundsAggregation.get(String.valueOf(BOUNDS));
            JsonNode topLeft = bounds.get(String.valueOf(TOP_LEFT));
            JsonNode bottomRight = bounds.get(String.valueOf(BOTTOM_RIGHT));

            topLeftLat = topLeft.get(String.valueOf(LAT)).asDouble();
            topLeftLon = topLeft.get(String.valueOf(LON)).asDouble();
            bottomRightLat = bottomRight.get(String.valueOf(LAT)).asDouble();
            bottomRightLon = bottomRight.get(String.valueOf(LON)).asDouble();
        }
    }

    /**
     * @return Top left latitude if bounds exist, null otherwise
     */
    public Double getTopLeftLat() {
        return topLeftLat;
    }

    /**
     * @return Top left longitude if bounds exist, null otherwise
     */
    public Double getTopLeftLon() {
        return topLeftLon;
    }

    /**
     * @return Bottom right latitude if bounds exist, null otherwise
     */
    public Double getBottomRightLat() {
        return bottomRightLat;
    }

    /**
     * @return Bottom right longitude if bounds exist, null otherwise
     */
    public Double getBottomRightLon() {
        return bottomRightLon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        GeoBoundsAggregation rhs = (GeoBoundsAggregation) obj;
        return super.equals(obj)
                && Objects.equals(topLeftLat, rhs.topLeftLat)
                && Objects.equals(topLeftLon, rhs.topLeftLon)
                && Objects.equals(bottomRightLat, rhs.bottomRightLat)
                && Objects.equals(bottomRightLon, rhs.bottomRightLon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                bottomRightLat,
                bottomRightLon,
                topLeftLat,
                topLeftLon);
    }
}
