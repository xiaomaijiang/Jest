package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * @author cfstout
 */
public class SumAggregation extends SingleValueAggregation {

    public static final String TYPE = "sum";

    public SumAggregation(String name, JsonNode sumAggregation) {
        super(name, sumAggregation);
    }

    public Double getSum() {
        return getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), TYPE);
    }
}
