package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author cfstout
 */
public class AvgAggregation extends SingleValueAggregation {

    public static final String TYPE = "avg";

    public AvgAggregation(String name, JsonNode avgAggregation) {
        super(name, avgAggregation);
    }

    /**
     * @return Average if it was found and not null, null otherwise
     */
    public Double getAvg() {
        return getValue();
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

        return super.equals(obj);
    }
}
