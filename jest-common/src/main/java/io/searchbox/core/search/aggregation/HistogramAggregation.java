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
public class HistogramAggregation extends BucketAggregation {

    public static final String TYPE = "histogram";

    private List<Histogram> histograms = new LinkedList<Histogram>();

    public HistogramAggregation(String name, JsonNode histogramAggregation) {
        super(name, histogramAggregation);
        if(histogramAggregation.has(String.valueOf(BUCKETS)) && histogramAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(histogramAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode bucketsSource) {
        for (JsonNode bucket : bucketsSource) {
            Histogram histogram = new Histogram(
                    bucket,
                    bucket.get(String.valueOf(KEY)).asLong(),
                    bucket.get(String.valueOf(DOC_COUNT)).asLong());
            histograms.add(histogram);
        }
    }

    public List<Histogram> getBuckets() {
        return histograms;
    }

    public static class Histogram extends Bucket {

        private Long key;

        Histogram(JsonNode bucket, Long key, Long count) {
            super(bucket, count);
            this.key = key;
        }

        public Long getKey() {
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

            Histogram rhs = (Histogram) obj;
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

        HistogramAggregation rhs = (HistogramAggregation) obj;
        return super.equals(obj) && Objects.equals(histograms, rhs.histograms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), histograms);
    }
}
