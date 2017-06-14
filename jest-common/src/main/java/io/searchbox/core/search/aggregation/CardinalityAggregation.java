package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.VALUE;

/**
 * @author cfstout
 */
public class CardinalityAggregation extends MetricAggregation {

    public static final String TYPE = "cardinality";

    private Long cardinality;

    public CardinalityAggregation(String name, JsonNode cardinalityAggregation) {
        super(name, cardinalityAggregation);
        cardinality = cardinalityAggregation.get(String.valueOf(VALUE)).asLong();
    }

    /**
     * @return Cardinality if it was found and not null, null otherwise
     */
    public Long getCardinality() {
        return cardinality;
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

        CardinalityAggregation rhs = (CardinalityAggregation) obj;
        return super.equals(obj) && Objects.equals(cardinality, rhs.cardinality);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cardinality);
    }
}

