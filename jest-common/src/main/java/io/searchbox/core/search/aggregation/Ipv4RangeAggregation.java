package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.FROM;
import static io.searchbox.core.search.aggregation.AggregationField.FROM_AS_STRING;
import static io.searchbox.core.search.aggregation.AggregationField.TO;
import static io.searchbox.core.search.aggregation.AggregationField.TO_AS_STRING;

/**
 * @author cfstout
 */
public class Ipv4RangeAggregation extends BucketAggregation{

    public static final String TYPE = "ip_range";

    private List<Ipv4Range> ranges = new LinkedList<Ipv4Range>();

    public Ipv4RangeAggregation(String name, JsonNode ipv4RangeAggregation) {
        super(name, ipv4RangeAggregation);
        if(ipv4RangeAggregation.has(String.valueOf(BUCKETS)) && ipv4RangeAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(ipv4RangeAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode bucketsSource) {
        for (JsonNode bucket : bucketsSource) {
            Ipv4Range range = new Ipv4Range(
                    bucket,
                    bucket.has(String.valueOf(FROM)) ? bucket.get(String.valueOf(FROM)).asDouble() : null,
                    bucket.has(String.valueOf(TO)) ? bucket.get(String.valueOf(TO)).asDouble() : null,
                    bucket.get(String.valueOf(DOC_COUNT)).asLong(),
                    bucket.has(String.valueOf(FROM_AS_STRING)) ? bucket.get(String.valueOf(FROM_AS_STRING)).asText() : null,
                    bucket.has(String.valueOf(TO_AS_STRING)) ? bucket.get(String.valueOf(TO_AS_STRING)).asText() : null);
            ranges.add(range);
        }
    }

    public List<Ipv4Range> getBuckets() {
        return ranges;
    }

    public class Ipv4Range extends Range {
        private String fromAsString;
        private String toAsString;

        public Ipv4Range(JsonNode bucket, Double from, Double to, Long count, String fromString, String toString){
            super(bucket, from, to, count);
            this.fromAsString = fromString;
            this.toAsString = toString;
        }

        public String getFromAsString() {
            return fromAsString;
        }

        public String getToAsString() {
            return toAsString;
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

            Ipv4Range rhs = (Ipv4Range) obj;
            return super.equals(obj)
                    && Objects.equals(toAsString, rhs.toAsString)
                    && Objects.equals(fromAsString, rhs.fromAsString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), toAsString, fromAsString);
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

        Ipv4RangeAggregation rhs = (Ipv4RangeAggregation) obj;
        return super.equals(obj) && Objects.equals(ranges, rhs.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ranges);
    }
}
