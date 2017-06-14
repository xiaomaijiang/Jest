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
public class DateRangeAggregation extends BucketAggregation {

    public static final String TYPE = "date_range";

    private List<DateRange> ranges = new LinkedList<DateRange>();

    public DateRangeAggregation(String name, JsonNode dateRangeAggregation) {
        super(name, dateRangeAggregation);
        if (dateRangeAggregation.has(String.valueOf(BUCKETS)) && dateRangeAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(dateRangeAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode buckets) {
        //todo support keyed:true as well
        for (JsonNode bucket : buckets) {
            DateRange range = new DateRange(
                    bucket,
                    bucket.has(String.valueOf(FROM)) ? bucket.get(String.valueOf(FROM)).asDouble() : null,
                    bucket.has(String.valueOf(TO)) ? bucket.get(String.valueOf(TO)).asDouble() : null,
                    bucket.get(String.valueOf(DOC_COUNT)).asLong(),
                    bucket.has(String.valueOf(FROM_AS_STRING)) ? bucket.get(String.valueOf(FROM_AS_STRING)).asText() : null,
                    bucket.has(String.valueOf(TO_AS_STRING)) ? bucket.get(String.valueOf(TO_AS_STRING)).asText() : null);
            ranges.add(range);
        }
    }

    public List<DateRange> getBuckets() {
        return ranges;
    }

    public class DateRange extends Range {
        private String fromAsString;
        private String toAsString;

        public DateRange(JsonNode bucket, Double from, Double to, Long count, String fromString, String toString) {
            super(bucket, from, to, count);
            this.fromAsString = fromString;
            this.toAsString = toString;
        }

        /**
         * @return From time as a string, or null if not specified
         */
        public String getFromAsString() {
            return fromAsString;
        }

        /**
         * @return To time as a string, or null if not specified
         */
        public String getToAsString() {
            return toAsString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DateRange)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            DateRange rhs = (DateRange) o;
            return Objects.equals(fromAsString, rhs.fromAsString) && Objects.equals(toAsString, rhs.toAsString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), fromAsString, toAsString);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DateRangeAggregation)) {
            return false;
        }

        DateRangeAggregation rhs = (DateRangeAggregation) o;
        return Objects.equals(getBuckets(), rhs.getBuckets());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBuckets());
    }
}
