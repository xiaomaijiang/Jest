package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * @author cfstout
 */
public class MaxAggregation extends SingleValueAggregation {

    public static final String TYPE = "max";

    public MaxAggregation(String name, JsonNode maxAggregation) {
        super(name, maxAggregation);
    }

    /**
     * @return Max if it was found and not null, null otherwise
     */
    public Double getMax() {
        return getValue();
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), TYPE);
    }

}
