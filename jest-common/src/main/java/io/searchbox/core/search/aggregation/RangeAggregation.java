package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.FROM;
import static io.searchbox.core.search.aggregation.AggregationField.TO;

/**
 * @author cfstout
 */
public class RangeAggregation extends BucketAggregation {

    public static final String TYPE = "range";

    private List<Range> ranges;

    public RangeAggregation(String name, JsonNode rangeAggregation) {
        super(name, rangeAggregation);
        ranges = new ArrayList<>();
        //todo support keyed:true as well
        for (JsonNode bucket : rangeAggregation.get(String.valueOf(BUCKETS))) {
            Range range = new Range(
                    bucket,
                    bucket.has(String.valueOf(FROM)) ? bucket.get(String.valueOf(FROM)).asDouble() : null,
                    bucket.has(String.valueOf(TO)) ? bucket.get(String.valueOf(TO)).asDouble() : null,
                    bucket.get(String.valueOf(DOC_COUNT)).asLong());
            ranges.add(range);
        }
    }

    public List<Range> getBuckets() {
        return ranges;
    }

    public String getName() {
        return name;
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

        RangeAggregation rhs = (RangeAggregation) obj;
        return super.equals(obj) && Objects.equals(ranges, rhs.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ranges);
    }
}
