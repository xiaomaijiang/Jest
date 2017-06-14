package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * @author cfstout
 */
public class ScriptedMetricAggregation extends SingleValueAggregation {

    public static final String TYPE = "scripted_metric";

    public ScriptedMetricAggregation(String name, JsonNode scriptedMetricAggregation) {
        super(name, scriptedMetricAggregation);
    }

    public Double getScriptedMetric() {
        return getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), TYPE);
    }

}
