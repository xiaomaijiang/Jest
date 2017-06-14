package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.core.SearchResult;

public class TopHitsAggregation extends SearchResult {

    protected String name;
    protected JsonNode jsonRoot;
    public static final String TYPE = "top_hits";

    public TopHitsAggregation(String name, JsonNode topHitAggregation) {
        // FIXME: Where to get a ObjectMapper from?
        super(new ObjectMapper());
        this.name = name;

        this.setSucceeded(true);
        this.setJsonObject(topHitAggregation);
        //this.setJsonString(topHitAggregation.asText());
        this.setPathToResult("hits/hits/_source");
    }

    public String getName() {
        return name;
    }
}
