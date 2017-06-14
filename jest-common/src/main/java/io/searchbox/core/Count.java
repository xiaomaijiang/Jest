package io.searchbox.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.action.AbstractAction;
import io.searchbox.action.AbstractMultiTypeActionBuilder;

import java.io.IOException;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
public class Count extends AbstractAction<CountResult> {

    protected Count(Builder builder) {
        super(builder);

        this.payload = builder.query;
        setURI(buildURI());
    }

    @Override
    protected String buildURI() {
        return super.buildURI() + "/_count";
    }

    @Override
    public String getPathToResult() {
        return "count";
    }

    @Override
    public CountResult createNewElasticSearchResult(String responseBody, int statusCode, String reasonPhrase, ObjectMapper objectMapper) throws IOException {
        return createNewElasticSearchResult(new CountResult(objectMapper), responseBody, statusCode, reasonPhrase, objectMapper);
    }

    @Override
    public String getRestMethodName() {
        return "POST";
    }

    public static class Builder extends AbstractMultiTypeActionBuilder<Count, Builder> {
        private String query;

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        @Override
        public Count build() {
            return new Count(this);
        }
    }
}
