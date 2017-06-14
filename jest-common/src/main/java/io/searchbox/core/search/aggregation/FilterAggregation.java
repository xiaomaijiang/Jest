package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;

/**
 * @author cfstout
 */
public class FilterAggregation extends Bucket {

    public static final String TYPE = "filter";

    public FilterAggregation(String name, JsonNode filterAggregation) {
        super(name, filterAggregation, filterAggregation.get(String.valueOf(DOC_COUNT)).asLong());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), TYPE);
    }

}
