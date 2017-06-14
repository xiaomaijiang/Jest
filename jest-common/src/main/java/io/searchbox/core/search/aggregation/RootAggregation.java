package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * Place holder class used to represent the root aggregation
 * returned to the user for processing.
 *
 * @author cfstout
 */
public class RootAggregation extends MetricAggregation {

    public RootAggregation(String name, JsonNode root) {
        super(name, root);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), "root");
    }
}
