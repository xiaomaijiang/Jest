package io.searchbox.core.search.sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * @author Riccardo Tasso
 * @author cihat keser
 */
public class Sort {
    // TODO:
    // * Geo Distance Sorting (Lat Lon as Properties, Lat Lon as String, Geohash, Lat Lon as Array)
    // * Script Based Sorting
    // * Track Scores (it should be in the Search object)

    private String field;
    private Sorting order;
    private Object missing;
    private Boolean unmapped;

    public Sort(String field) {
        this.field = field;
    }

    public Sort(String field, Sorting order) {
        this.field = field;
        this.order = order;
    }

    /**
     * @param m should be a Missing object (LAST or FIRST) or a custom value
     *          (String, Integer, Double, ...) that will be used for missing docs as the sort value
     */
    public void setMissing(Object m) {
        this.missing = m;
    }

    public void setIgnoreUnmapped() {
        this.unmapped = true;
    }

    public JsonNode toJsonObject(ObjectMapper objectMapper) {
        final ObjectNode sortDefinition = objectMapper.createObjectNode();
        if (order != null) {
            sortDefinition.set("order", new TextNode(order.toString()));
        }
        if (missing != null) {
            sortDefinition.set("missing", new TextNode(missing.toString()));
        }
        if (unmapped != null) {
            sortDefinition.set("ignore_unmapped", BooleanNode.valueOf(unmapped));
        }

        final ObjectNode sortObject = objectMapper.createObjectNode();
        sortObject.set(field, sortDefinition);

        return sortObject;
    }

    public enum Sorting {
        ASC("asc"),
        DESC("desc");

        private final String name;

        private Sorting(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    public enum Missing {
        LAST("_last"),
        FIRST("_first");

        private final String name;

        private Missing(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

}