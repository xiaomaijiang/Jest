package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.VALUES;

/**
 * @author cfstout
 */
public class PercentileRanksAggregation extends MetricAggregation {

    public static final String TYPE = "percentile_ranks";

    private Map<String, Double> percentileRanks = new HashMap<String, Double>();

    public PercentileRanksAggregation(String name, JsonNode percentilesAggregation) {
        super(name, percentilesAggregation);
        parseSource(percentilesAggregation.get(String.valueOf(VALUES)));
    }

    private void parseSource(JsonNode source) {
        final Iterator<Map.Entry<String, JsonNode>> it = source.fields();
        while (it.hasNext()) {
            final Map.Entry<String, JsonNode> entry = it.next();
            if (!(Double.isNaN(entry.getValue().asDouble()))) {
                percentileRanks.put(entry.getKey(), entry.getValue().asDouble());
            }
        }
    }

    public Map<String, Double> getPercentileRanks() {
        return percentileRanks;
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

        PercentileRanksAggregation rhs = (PercentileRanksAggregation) obj;
        return super.equals(obj) && Objects.equals(percentileRanks, rhs.percentileRanks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), percentileRanks);
    }

}
