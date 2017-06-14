package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import io.searchbox.action.AbstractAction;
import io.searchbox.action.AbstractMultiIndexActionBuilder;
import io.searchbox.action.AbstractMultiTypeActionBuilder;

import java.io.IOException;

/**
 * @author Bartosz Polnik
 */
public class Cat extends AbstractAction<CatResult> {
    private final static String PATH_TO_RESULT = "result";
    private final String operationPath;

    protected <T extends AbstractAction.Builder<Cat, ? extends Builder> & CatBuilder> Cat(T builder) {
        super(builder);
        this.operationPath = builder.operationPath();
        setURI(buildURI());
    }

    @Override
    protected String buildURI() {
        String uriSuffix = super.buildURI();
        return "_cat/" + this.operationPath + (uriSuffix.isEmpty() ? "" : "/") + uriSuffix;
    }

    @Override
    public String getRestMethodName() {
        return "GET";
    }

    @Override
    public String getPathToResult() {
        return PATH_TO_RESULT;
    }

    @Override
    public CatResult createNewElasticSearchResult(String responseBody, int statusCode, String reasonPhrase, ObjectMapper objectMapper) throws IOException {
        return createNewElasticSearchResult(new CatResult(objectMapper), responseBody, statusCode, reasonPhrase, objectMapper);
    }

    @Override
    protected ObjectNode parseResponseBody(String responseBody, ObjectMapper objectMapper) throws IOException {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return objectMapper.createObjectNode();
        }

        final JsonNode parsed = objectMapper.readTree(responseBody);

        if (parsed.isArray()) {
            ObjectNode result = objectMapper.createObjectNode();
            result.set(PATH_TO_RESULT, parsed);
            return result;
        } else {
            // TODO: Specific exception?
            throw new IllegalArgumentException("Cat response did not contain a JSON Array");
        }
    }

    public static class IndicesBuilder extends AbstractMultiTypeActionBuilder<Cat, IndicesBuilder> implements CatBuilder {
        private static final String operationPath = "indices";

        public IndicesBuilder() {
            setHeader("accept", "application/json");
            setHeader("content-type", "application/json");
        }

        @Override
        public Cat build() {
            return new Cat(this);
        }

        @Override
        public String operationPath() {
            return operationPath;
        }
    }

    public static class AliasesBuilder extends AbstractMultiIndexActionBuilder<Cat, AliasesBuilder> implements CatBuilder {
        private static final String operationPath = "aliases";
        public AliasesBuilder() {
            setHeader("accept", "application/json");
            setHeader("content-type", "application/json");
        }

        @Override
        public Cat build() {
            return new Cat(this);
        }

        @Override
        public String operationPath() {
            return operationPath;
        }
    }

    public static class ShardsBuilder extends AbstractMultiIndexActionBuilder<Cat, ShardsBuilder> implements CatBuilder {
        private static final String operationPath = "shards";
        public ShardsBuilder() {
            setHeader("accept", "application/json");
            setHeader("content-type", "application/json");
        }

        @Override
        public Cat build() {
            return new Cat(this);
        }

        @Override
        public String operationPath() {
            return operationPath;
        }

        @Override
        public String getJoinedIndices() {
            if (indexNames.size() > 0) {
                return Joiner.on(',').join(indexNames);
            } else {
                return null;
            }
        }
    }

    public static class NodesBuilder extends AbstractAction.Builder<Cat, NodesBuilder> implements CatBuilder {
        private static final String operationPath = "nodes";
        public NodesBuilder() {
            setHeader("accept", "application/json");
            setHeader("content-type", "application/json");
        }

        @Override
        public Cat build() {
            return new Cat(this);
        }

        @Override
        public String operationPath() {
            return operationPath;
        }
    }

    protected interface CatBuilder {
        String operationPath();
    }
}
