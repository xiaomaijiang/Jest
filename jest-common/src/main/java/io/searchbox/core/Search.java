package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.searchbox.action.AbstractAction;
import io.searchbox.action.AbstractMultiTypeActionBuilder;
import io.searchbox.core.search.sort.Sort;
import io.searchbox.params.Parameters;
import io.searchbox.params.SearchType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
public class Search extends AbstractAction<SearchResult> {

    private String query;
    private List<Sort> sortList = new LinkedList<Sort>();
    protected List<String> includePatternList = new ArrayList<String>();
    protected List<String> excludePatternList = new ArrayList<String>();

    protected Search(Builder builder) {
        super(builder);

        this.query = builder.query;
        this.sortList = builder.sortList;
        this.includePatternList = builder.includePatternList;
        this.excludePatternList = builder.excludePatternList;
        setURI(buildURI());
    }

    protected Search(TemplateBuilder templatedBuilder) {
        super(templatedBuilder);

        //reuse query as it's just the request body of the POST
        this.query = templatedBuilder.query;
        this.sortList = templatedBuilder.sortList;
        this.includePatternList = templatedBuilder.includePatternList;
        this.excludePatternList = templatedBuilder.excludePatternList;
        setURI(buildURI() + "/template");
    }

    @Override
    public SearchResult createNewElasticSearchResult(String responseBody, int statusCode, String reasonPhrase, ObjectMapper objectMapper) throws IOException {
        return createNewElasticSearchResult(new SearchResult(objectMapper), responseBody, statusCode, reasonPhrase, objectMapper);
    }

    public String getIndex() {
        return this.indexName;
    }

    public String getType() {
        return this.typeName;
    }

    @Override
    protected String buildURI() {
        return super.buildURI() + "/_search";
    }

    @Override
    public String getPathToResult() {
        return "hits/hits/_source";
    }

    @Override
    public String getRestMethodName() {
        return "POST";
    }

    @Override
    public String getData(ObjectMapper objectMapper) throws IOException {
        String data;
        if (sortList.isEmpty() && includePatternList.isEmpty() && excludePatternList.isEmpty()) {
            data = query;
        } else {
            ObjectNode queryObject = (ObjectNode) objectMapper.readTree(query);

            if (queryObject == null) {
                queryObject = objectMapper.createObjectNode();
            }

            if (!sortList.isEmpty()) {
                ArrayNode sortArray = normalizeSortClause(queryObject, objectMapper);

                for (Sort sort : sortList) {
                    sortArray.add(sort.toJsonObject(objectMapper));
                }
            }

            if (!includePatternList.isEmpty() || !excludePatternList.isEmpty()) {
                ObjectNode sourceObject = normalizeSourceClause(queryObject, objectMapper);

                addPatternListToSource(sourceObject, "include", includePatternList, objectMapper);
                addPatternListToSource(sourceObject, "exclude", excludePatternList, objectMapper);
            }

            data = objectMapper.writeValueAsString(queryObject);
        }
        return data;
    }

    private static ArrayNode normalizeSortClause(ObjectNode queryObject, ObjectMapper objectMapper) {
        final ArrayNode sortArray;
        if (queryObject.has("sort")) {
            JsonNode sortElement = queryObject.get("sort");
            if (sortElement.isArray()) {
                sortArray = (ArrayNode) sortElement;
            } else if (sortElement.isObject()) {
                sortArray = objectMapper.createArrayNode();
                sortArray.add(sortElement);
            } else if (sortElement.isTextual()) {
                String sortField = sortElement.asText();
                sortArray = objectMapper.createArrayNode();
                queryObject.set("sort", sortArray);
                String order;
                if ("_score".equals(sortField)) {
                    order = "desc";
                } else {
                    order = "asc";
                }
                ObjectNode sortOrder = objectMapper.createObjectNode();
                sortOrder.set("order", new TextNode(order));
                ObjectNode sortDefinition = objectMapper.createObjectNode();
                sortDefinition.set(sortField, sortOrder);

                sortArray.add(sortDefinition);
            } else {
                // TODO: Specific exception?
                throw new IllegalArgumentException("_source must be an array, an object or a string");
            }
        } else {
            sortArray = objectMapper.createArrayNode();
        }
        queryObject.set("sort", sortArray);

        return sortArray;
    }

    private static ObjectNode normalizeSourceClause(ObjectNode queryObject, ObjectMapper objectMapper) {
        ObjectNode sourceObject;
        if (queryObject.has("_source")) {
            JsonNode sourceElement = queryObject.get("_source");

            if (sourceElement.isObject()) {
                sourceObject = (ObjectNode) sourceElement;
            } else if (sourceElement.isArray()) {
                // in this case, the values of the array are includes
                sourceObject = objectMapper.createObjectNode();
                queryObject.set("_source", sourceObject);
                sourceObject.set("include", sourceElement);
            } else if (sourceElement.isBoolean()) {
                // if _source is a boolean, we override the configuration with include/exclude
                sourceObject = objectMapper.createObjectNode();
            } else {
                throw new IllegalArgumentException("_source must be an object, an array or a boolean");
            }
        } else {
            sourceObject = objectMapper.createObjectNode();
        }
        queryObject.set("_source", sourceObject);

        return sourceObject;
    }

    private static void addPatternListToSource(ObjectNode sourceObject, String rule, List<String> patternList, ObjectMapper objectMapper) {
        if (!patternList.isEmpty()) {
            ArrayNode ruleArray;
            if (sourceObject.has(rule)) {
                ruleArray = (ArrayNode) sourceObject.get(rule);
            } else {
                ruleArray = objectMapper.createArrayNode();
                sourceObject.set(rule, ruleArray);
            }
            for (String pattern : patternList) {
                ruleArray.add(pattern);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), query, sortList, includePatternList, excludePatternList);
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

        Search rhs = (Search) obj;
        return super.equals(obj)
                && Objects.equals(query, rhs.query)
                && Objects.equals(sortList, rhs.sortList)
                && Objects.equals(includePatternList, rhs.includePatternList)
                && Objects.equals(excludePatternList, rhs.excludePatternList);
    }

    public static class Builder extends AbstractMultiTypeActionBuilder<Search, Builder> {
        protected String query;
        protected List<Sort> sortList = new LinkedList<Sort>();
        protected List<String> includePatternList = new ArrayList<String>();
        protected List<String> excludePatternList = new ArrayList<String>();

        public Builder(String query) {
            this.query = query;
        }

        public Builder setSearchType(SearchType searchType) {
            return setParameter(Parameters.SEARCH_TYPE, searchType);
        }

        public Builder addSort(Sort sort) {
            sortList.add(sort);
            return this;
        }

        public Builder addSourceExcludePattern(String excludePattern) {
            excludePatternList.add(excludePattern);
            return this;
        }

        public Builder addSourceIncludePattern(String includePattern) {
            includePatternList.add(includePattern);
            return this;
        }

        public Builder addSort(Collection<Sort> sorts) {
            sortList.addAll(sorts);
            return this;
        }

        @Override
        public Search build() {
            return new Search(this);
        }
    }

    public static class VersionBuilder extends Builder {
        public VersionBuilder(String query) {
            super(query);
            this.setParameter(Parameters.VERSION, "true");
        }
    }

    public static class TemplateBuilder extends Builder {
    	public TemplateBuilder(String templatedQuery) {
            super(templatedQuery);
        }

    	@Override
        public Search build() {
            return new Search(this);
        }
    }
}
