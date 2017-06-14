package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author cfstout
 */
public class PercentilesAggregation extends MetricAggregation {

    public static final String TYPE = "percentiles";

    private Map<String, Double> percentiles = new HashMap<String, Double>();

    public PercentilesAggregation(String name, JsonNode percentilesAggregation) {
        super(name, percentilesAggregation);
        parseSource(percentilesAggregation.get(String.valueOf(AggregationField.VALUES)));
    }

    private void parseSource(JsonNode source) {
        final Iterator<Map.Entry<String, JsonNode>> it = source.fields();
        while (it.hasNext()) {
            final Map.Entry<String, JsonNode> entry = it.next();
            if (!(Double.isNaN(entry.getValue().asDouble()))) {
                percentiles.put(entry.getKey(), entry.getValue().asDouble());
            }
        }
    }

    public Map<String, Double> getPercentiles() {
        return percentiles;
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

        PercentilesAggregation rhs = (PercentilesAggregation) obj;
        return super.equals(obj) && Objects.equals(percentiles, rhs.percentiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), percentiles);
    }

}
