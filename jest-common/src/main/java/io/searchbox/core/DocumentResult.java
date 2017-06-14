package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.client.JestResult;

/**
 * @author Bartosz Polnik
 */
public class DocumentResult extends JestResult {
    public DocumentResult(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public String getIndex() {
        return getAsString(jsonObject.get("_index"));
    }

    public String getType() {
        return getAsString(jsonObject.get("_type"));
    }

    public String getId() {
        return getAsString(jsonObject.get("_id"));
    }

    public Long getVersion() {
        return getAsLong(jsonObject.get("_version"));
    }

    private String getAsString(JsonNode jsonElement) {
        if(jsonElement == null) {
            return null;
        } else {
            return jsonElement.asText();
        }
    }

    private Long getAsLong(JsonNode jsonElement) {
        if(jsonElement == null) {
            return null;
        } else {
            return jsonElement.asLong();
        }
    }

}
