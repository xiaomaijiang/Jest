package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * @author cfstout
 */
public class MinAggregation extends SingleValueAggregation {

    public static final String TYPE = "min";

    public MinAggregation(String name, JsonNode minAggregation) {
        super(name, minAggregation);
    }

    /**
     * @return Min if it was found and not null, null otherwise
     */
    public Double getMin() {
        return getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), TYPE);
    }
}
