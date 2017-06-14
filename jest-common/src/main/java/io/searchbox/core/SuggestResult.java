package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.client.JestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author cihat keser
 */
public class SuggestResult extends JestResult {

    public SuggestResult(SuggestResult suggestResult) {
        super(suggestResult);
    }

    public SuggestResult(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public List<Suggestion> getSuggestions(String suggestionName) {
        List<Suggestion> suggestions = new ArrayList<Suggestion>();

        if (jsonObject != null && jsonObject.has(suggestionName)) {
            for (JsonNode suggestionElement : jsonObject.get(suggestionName)) {
                final Suggestion suggestion = objectMapper.convertValue(suggestionElement, Suggestion.class);
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    public static class Suggestion {
        public String text;
        public Integer offset;
        public Integer length;
        public List<Map<String, Object>> options;

        public Suggestion() {

        }

        public Suggestion(String text, Integer offset, Integer length, List<Map<String, Object>> options) {
            this.text = text;
            this.offset = offset;
            this.length = length;
            this.options = options;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    text,
                    offset,
                    length,
                    options);
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

            Suggestion rhs = (Suggestion) obj;
            return Objects.equals(text, rhs.text)
                    && Objects.equals(offset, rhs.offset)
                    && Objects.equals(length, rhs.length)
                    && Objects.equals(options, rhs.options);
        }
    }

}
