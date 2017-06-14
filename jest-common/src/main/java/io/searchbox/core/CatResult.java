package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import io.searchbox.client.JestResult;

import java.util.List;
import java.util.Map;

/**
* @author Bartosz Polnik
*/
public class CatResult extends JestResult {

    public CatResult(CatResult catResult) {
        super(catResult);
    }

    public CatResult(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    /**
     *
     * @return empty array if response is not present, otherwise column names as first row plus one additional row per single result
     */
    public String[][] getPlainText() {
        final JsonNode jsonObject = getJsonObject();
        if(jsonObject != null && jsonObject.has(getPathToResult()) && jsonObject.get(getPathToResult()).isArray()) {
            ArrayNode esResultRows = (ArrayNode) jsonObject.get(getPathToResult());
            if(esResultRows.size() > 0 && esResultRows.get(0).isObject()) {
                return parseResultArray(esResultRows);
            }
        }

        return new String[0][0];
    }

    private String[][] parseResultArray(ArrayNode esResponse) {
        final JsonNode jsonNode = esResponse.get(0);
        List<Map.Entry<String, JsonNode>> fieldsInFirstResponseRow = Lists.newArrayList(jsonNode.fields());
        String[][] result = new String[esResponse.size() + 1][fieldsInFirstResponseRow.size()];
        for(int i = 0; i < fieldsInFirstResponseRow.size(); i++) {
            result[0][i] = fieldsInFirstResponseRow.get(i).getKey();
        }

        int rowNum = 1;
        for(JsonNode row: esResponse) {
            for (int colId = 0; colId < fieldsInFirstResponseRow.size(); colId++) {
                result[rowNum][colId] = row.get(result[0][colId]).asText();
            }

            rowNum++;
        }

        return result;
    }
}
