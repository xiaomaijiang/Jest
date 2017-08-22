package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import io.searchbox.client.JestResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.RootAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author cihat keser
 */
public class SearchResult extends JestResult {

    public static final String EXPLANATION_KEY = "_explanation";
    public static final String HIGHLIGHT_KEY = "highlight";
    public static final String SORT_KEY = "sort";
    public static final String[] PATH_TO_TOTAL = "hits/total".split("/");
    public static final String[] PATH_TO_MAX_SCORE = "hits/max_score".split("/");

    public SearchResult(SearchResult searchResult) {
        super(searchResult);
    }

    public SearchResult(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    @Deprecated
    public <T> T getSourceAsObject(Class<T> clazz) {
        return super.getSourceAsObject(clazz);
    }

    @Override
    @Deprecated
    public <T> List<T> getSourceAsObjectList(Class<T> type) {
        return super.getSourceAsObjectList(type);
    }

    public <T> Hit<T, Void> getFirstHit(Class<T> sourceType) {
        return getFirstHit(sourceType, Void.class);
    }

    public <T, K> Hit<T, K> getFirstHit(Class<T> sourceType, Class<K> explanationType) {
        Hit<T, K> hit = null;

        List<Hit<T, K>> hits = getHits(sourceType, explanationType, true);
        if (!hits.isEmpty()) {
            hit = hits.get(0);
        }

        return hit;
    }

    public <T> List<Hit<T, Void>> getHits(Class<T> sourceType) {
        return getHits(sourceType, true);
    }

    public <T> List<Hit<T, Void>> getHits(Class<T> sourceType, boolean addEsMetadataFields) {
        return getHits(sourceType, Void.class, addEsMetadataFields);
    }

    public <T, K> List<Hit<T, K>> getHits(Class<T> sourceType, Class<K> explanationType) {
        return getHits(sourceType, explanationType, false, true);
    }

    public <T, K> List<Hit<T, K>> getHits(Class<T> sourceType, Class<K> explanationType, boolean addEsMetadataFields) {
        return getHits(sourceType, explanationType, false, addEsMetadataFields);
    }

    protected <T, K> List<Hit<T, K>> getHits(Class<T> sourceType, Class<K> explanationType, boolean returnSingle, boolean addEsMetadataFields) {
        List<Hit<T, K>> sourceList = new ArrayList<Hit<T, K>>();

        if (jsonObject != null) {
            String[] keys = getKeys();
            if (keys != null) { // keys would never be null in a standard search scenario (i.e.: unless search class is overwritten)
                String sourceKey = keys[keys.length - 1];
                JsonNode obj = jsonObject.get(keys[0]);
                for (int i = 1; i < keys.length - 1; i++) {
                    obj = obj.get(keys[i]);
                }

                if (obj.isObject()) {
                    sourceList.add(extractHit(sourceType, explanationType, obj, sourceKey, addEsMetadataFields));
                } else if (obj.isArray()) {
                    for (JsonNode hitElement : obj) {
                        sourceList.add(extractHit(sourceType, explanationType, hitElement, sourceKey, addEsMetadataFields));
                        if (returnSingle) break;
                    }
                }
            }
        }

        return sourceList;
    }

    protected <T, K> Hit<T, K> extractHit(Class<T> sourceType, Class<K> explanationType, JsonNode hitElement, String sourceKey, boolean addEsMetadataFields) {
        Hit<T, K> hit = null;

        if (hitElement.isObject()) {
            ObjectNode hitObject = (ObjectNode) hitElement;
            ObjectNode source = (ObjectNode) hitObject.get(sourceKey);

            if (source != null) {
                String index = hitObject.get("_index").asText();
                String type = hitObject.get("_type").asText();

                String id = hitObject.get("_id").asText();

                Double score = null;
                if (hitObject.has("_score") && !hitObject.get("_score").isNull()) {
                    score = hitObject.get("_score").asDouble();
                }

                JsonNode explanation = hitObject.get(EXPLANATION_KEY);
                Map<String, List<String>> highlight = extractHighlight((ObjectNode) hitObject.get(HIGHLIGHT_KEY));
                List<String> sort = extractSort(hitObject.get(SORT_KEY));

                if (addEsMetadataFields) {
                    ObjectNode clonedSource = null;
                    for (MetaField metaField : META_FIELDS) {
                        JsonNode metaElement = hitObject.get(metaField.esFieldName);
                        if (metaElement != null) {
                            if (clonedSource == null) {
                                clonedSource = source.deepCopy();
                            }
                            clonedSource.set(metaField.internalFieldName, metaElement);
                        }
                    }
                    if (clonedSource != null) {
                        source = clonedSource;
                    }
                }

                hit = new Hit<T, K>(
                        sourceType,
                        source,
                        explanationType,
                        explanation,
                        highlight,
                        sort,
                        index,
                        type,
                        id,
                        score
                );
            }
        }

        return hit;
    }

    protected List<String> extractSort(JsonNode sort) {
        if (sort == null) {
            return null;
        }

        List<String> retval = new ArrayList<String>(sort.size());
        for (JsonNode sortValue : sort) {
            retval.add(sortValue.isNull() ? "" : sortValue.asText());
        }
        return retval;
    }

    protected Map<String, List<String>> extractHighlight(ObjectNode highlight) {
        Map<String, List<String>> retval = null;

        if (highlight != null) {
            Set<Map.Entry<String, JsonNode>> highlightSet = Sets.newHashSet(highlight.fields());
            retval = new HashMap<String, List<String>>(highlightSet.size());

            for (Map.Entry<String, JsonNode> entry : highlightSet) {
                List<String> fragments = new ArrayList<String>();
                for (JsonNode element : entry.getValue()) {
                    fragments.add(element.asText());
                }
                retval.put(entry.getKey(), fragments);
            }
        }

        return retval;
    }

    public Long getTotal() {
        Long total = null;
        JsonNode obj = getPath(PATH_TO_TOTAL);
        if (obj != null) total = obj.asLong();
        return total;
    }

    public Float getMaxScore() {
        Float maxScore = null;
        JsonNode obj = getPath(PATH_TO_MAX_SCORE);
        if (obj != null) maxScore = obj.floatValue();
        return maxScore;
    }

    protected JsonNode getPath(String[] path) {
        JsonNode retval = null;
        if (jsonObject != null) {
            JsonNode obj = jsonObject;
            for (String component : path) {
                if (obj == null) break;
                obj = ((ObjectNode) obj).get(component);
            }
            retval = obj;
        }
        return retval;
    }

    public MetricAggregation getAggregations() {
        final String rootAggrgationName = "aggs";
        if (jsonObject == null) return new RootAggregation(rootAggrgationName, objectMapper.createObjectNode());
        if (jsonObject.has("aggregations"))
            return new RootAggregation(rootAggrgationName, jsonObject.get("aggregations"));
        if (jsonObject.has("aggs")) return new RootAggregation(rootAggrgationName, jsonObject.get("aggs"));

        return new RootAggregation(rootAggrgationName, objectMapper.createObjectNode());
    }

    /**
     * Immutable class representing a search hit.
     *
     * @param <T> type of source
     * @param <K> type of explanation
     * @author cihat keser
     */
    public class Hit<T, K> {

        public final T source;
        public final K explanation;
        public final Map<String, List<String>> highlight;
        public final List<String> sort;
        public final String index;
        public final String type;
        public final String id;
        public final Double score;

        public Hit(Class<T> sourceType, JsonNode source) {
            this(sourceType, source, null, null);
        }

        public Hit(Class<T> sourceType, JsonNode source, Class<K> explanationType, JsonNode explanation) {
            this(sourceType, source, explanationType, explanation, null, null);
        }

        public Hit(Class<T> sourceType, JsonNode source, Class<K> explanationType, JsonNode explanation,
                   Map<String, List<String>> highlight, List<String> sort) {
            this(sourceType, source, explanationType, explanation, highlight, sort, null, null, null, null);
        }

        public Hit(Class<T> sourceType, JsonNode source, Class<K> explanationType, JsonNode explanation,
                   Map<String, List<String>> highlight, List<String> sort, String index, String type, String id, Double score) {
            if (source == null) {
                this.source = null;
            } else {
                this.source = createSourceObject(source, sourceType);
            }
            if (explanation == null) {
                this.explanation = null;
            } else {
                this.explanation = createSourceObject(explanation, explanationType);
            }
            this.highlight = highlight;
            this.sort = sort;

            this.index = index;
            this.type = type;
            this.id = id;
            this.score = score;
        }

        public Hit(T source) {
            this(source, null, null, null);
        }

        public Hit(T source, K explanation) {
            this(source, explanation, null, null);
        }

        public Hit(T source, K explanation, Map<String, List<String>> highlight, List<String> sort) {
            this(source, explanation, highlight, sort, null, null, null, null);
        }

        public Hit(T source, K explanation, Map<String, List<String>> highlight, List<String> sort, String index, String type, String id, Double score) {
            this.source = source;
            this.explanation = explanation;
            this.highlight = highlight;
            this.sort = sort;

            this.index = index;
            this.type = type;
            this.id = id;
            this.score = score;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    source,
                    explanation,
                    highlight,
                    sort,
                    index,
                    type,
                    id);
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

            Hit rhs = (Hit) obj;
            return Objects.equals(source, rhs.source)
                    && Objects.equals(explanation, rhs.explanation)
                    && Objects.equals(highlight, rhs.highlight)
                    && Objects.equals(sort, rhs.sort)
                    && Objects.equals(index, rhs.index)
                    && Objects.equals(type, rhs.type)
                    && Objects.equals(id, rhs.id);
        }
    }

}
