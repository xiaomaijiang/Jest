package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * @author cfstout
 */
public abstract class BucketAggregation extends Aggregation {

    public BucketAggregation(String name, JsonNode root) {
        super(name, root);
    }

    abstract List<? extends Bucket> getBuckets();
}
