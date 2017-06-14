package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.KEY;

/**
 * @author cfstout
 */
public class GeoHashGridAggregation extends BucketAggregation{

    public static final String TYPE = "geohash_grid";

    private List<GeoHashGrid> geoHashGrids = new LinkedList<GeoHashGrid>();

    public GeoHashGridAggregation(String name, JsonNode geohashGridAggregation) {
        super(name, geohashGridAggregation);
        if(geohashGridAggregation.has(String.valueOf(BUCKETS)) && geohashGridAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(geohashGridAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode bucketsSource) {
        for (JsonNode bucket : bucketsSource) {
            GeoHashGrid geoHashGrid = new GeoHashGrid(
                    bucket,
                    bucket.get(String.valueOf(KEY)).asText(),
                    bucket.get(String.valueOf(DOC_COUNT)).asLong());
            geoHashGrids.add(geoHashGrid);
        }
    }

    public List<GeoHashGrid> getBuckets() {
        return geoHashGrids;
    }

    public static class GeoHashGrid extends Bucket {
        private String key;

        public GeoHashGrid(JsonNode bucket, String key, Long count) {
            super(bucket, count);
            this.key = key;
        }

        public String getKey() {
            return key;
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

            GeoHashGrid rhs = (GeoHashGrid) obj;
            return super.equals(obj) && Objects.equals(key, rhs.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), key);
        }
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

        GeoHashGridAggregation rhs = (GeoHashGridAggregation) obj;
        return super.equals(obj) && Objects.equals(geoHashGrids, rhs.geoHashGrids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), geoHashGrids);
    }
}
