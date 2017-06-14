package io.searchbox.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.action.AbstractAction;
import io.searchbox.action.AbstractMultiTypeActionBuilder;

import java.io.IOException;

public class Suggest extends AbstractAction<SuggestResult> {

    protected Suggest(final Builder builder) {
        super(builder);

        this.payload = builder.getQuery();
        this.setURI(this.buildURI());
    }

    @Override
    public SuggestResult createNewElasticSearchResult(final String json, final int statusCode, final String reasonPhrase, final ObjectMapper objectMapper) throws IOException {
        return this.createNewElasticSearchResult(new SuggestResult(objectMapper), json, statusCode, reasonPhrase, objectMapper);
    }

    @Override
    public String getRestMethodName() {
        return "POST";
    }

    public String getIndex() {
        return this.indexName;
    }

    public String getType() {
        return this.typeName;
    }

    @Override
    protected String buildURI() {
        return super.buildURI() + "/_suggest";
    }

    public static class Builder extends AbstractMultiTypeActionBuilder<Suggest, Builder> {

        private final String query;

        /**
         * Suggest requests executed against the _suggest endpoint should omit the
         * surrounding suggest element which is only used if the suggest request is part of a search.
         */
        public Builder(final String query) {
            this.query = query;
        }

        public final String getQuery() {
            return this.query;
        }

        @Override
        public Suggest build() {
            return new Suggest(this);
        }

    }

}
