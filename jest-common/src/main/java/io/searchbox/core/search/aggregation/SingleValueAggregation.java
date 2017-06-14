package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.VALUE;

/**
 * @author cfstout
 */
public abstract class SingleValueAggregation extends MetricAggregation {

    private Double value;

    protected SingleValueAggregation(String name, JsonNode singleValueAggregation) {
        super(name, singleValueAggregation);
        if(singleValueAggregation.has(String.valueOf(VALUE)) && !singleValueAggregation.get(String.valueOf(VALUE)).isNull()) {
            value = singleValueAggregation.get(String.valueOf(VALUE)).asDouble();
        }
    }

    /**
     * @return value if it was found and not null, null otherwise
     */
    protected Double getValue() {
        return value;
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

        SingleValueAggregation rhs = (SingleValueAggregation) obj;
        return super.equals(obj) && Objects.equals(value, rhs.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
