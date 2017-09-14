package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import io.searchbox.client.JestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cihat keser
 */
public class MultiSearchResult extends JestResult {
    private static final String RESPONSES_KEY = "responses";
    private static final String ERROR_KEY = "error";

    public MultiSearchResult(MultiSearchResult source) {
        super(source);
    }

    public MultiSearchResult(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public List<MultiSearchResponse> getResponses() {
        List<MultiSearchResponse> multiSearchResponses = new ArrayList<MultiSearchResponse>();

        if (jsonObject != null && jsonObject.has(RESPONSES_KEY)) {
            JsonNode responsesArray = jsonObject.get(RESPONSES_KEY);
            for (JsonNode responseElement : responsesArray) {
                multiSearchResponses.add(new MultiSearchResponse(responseElement));
            }
        }

        return multiSearchResponses;
    }

    public class MultiSearchResponse {

        public final boolean isError;
        public final String errorMessage;
        public final JsonNode error;
        public final SearchResult searchResult;

        public MultiSearchResponse(JsonNode jsonObject) {
            final JsonNode error = jsonObject.get(ERROR_KEY);
            if (error != null) {
                this.isError = true;
                this.error = error;
                this.errorMessage = error.path("reason").asText();
                this.searchResult = null;
            } else {
                this.isError = false;
                this.errorMessage = null;
                this.error = MissingNode.getInstance();

                this.searchResult = new SearchResult(objectMapper);
                this.searchResult.setSucceeded(true);
                this.searchResult.setResponseCode(responseCode);
                this.searchResult.setJsonObject(jsonObject);
                this.searchResult.setJsonString(jsonObject.toString());
                this.searchResult.setPathToResult("hits/hits/_source");
            }
        }
    }
}
