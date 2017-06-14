package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;

/**
 * @author cfstout
 */

public class MissingAggregation extends MetricAggregation {
    public static final String TYPE = "missing";

    private Long missing;

    public MissingAggregation(String name, JsonNode missingAggregation) {
        super(name, missingAggregation);
        missing = missingAggregation.get(String.valueOf(DOC_COUNT)).asLong();
    }

    public Long getMissing() {
        return missing;
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

        MissingAggregation rhs = (MissingAggregation) obj;
        return super.equals(obj) && Objects.equals(missing, rhs.missing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), missing);
    }
}
