package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.FROM;
import static io.searchbox.core.search.aggregation.AggregationField.TO;

/**
 * @author cfstout
 */
public class GeoDistanceAggregation extends BucketAggregation {

    public static final String TYPE = "geo_distance";

    private List<Range> geoDistances = new LinkedList<Range>();

    public GeoDistanceAggregation(String name, JsonNode geoDistanceAggregation) {
        super(name, geoDistanceAggregation);
        if (geoDistanceAggregation.has(String.valueOf(BUCKETS)) && geoDistanceAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(geoDistanceAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode buckets) {
        //todo support keyed:true as well
        for (JsonNode bucket : buckets) {
            Range geoDistance = new Range(
                    bucket,
                    bucket.has(String.valueOf(FROM)) ? bucket.get(String.valueOf(FROM)).asDouble() : null,
                    bucket.has(String.valueOf(TO)) ? bucket.get(String.valueOf(TO)).asDouble() : null,
                    bucket.has(String.valueOf(DOC_COUNT)) ? bucket.get(String.valueOf(DOC_COUNT)).asLong() : null);
            geoDistances.add(geoDistance);
        }
    }

    public List<Range> getBuckets() {
        return geoDistances;
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

        GeoDistanceAggregation rhs = (GeoDistanceAggregation) obj;
        return super.equals(obj) && Objects.equals(geoDistances, rhs.geoDistances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), geoDistances);
    }
}
