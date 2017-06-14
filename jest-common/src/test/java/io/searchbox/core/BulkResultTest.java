package io.searchbox.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BulkResultTest {

    static String indexFailedResult = "{\n" +
            "    \"took\": 10,\n" +
            "    \"errors\": true,\n" +
            "    \"items\": [\n" +
            "        {\n" +
            "            \"index\": {\n" +
            "                \"_index\": \"index-name\",\n" +
            "                \"_type\": \"type-name\",\n" +
            "                \"_id\": null,\n" +
            "                \"status\": 400,\n" +
            "                \"error\": \"MapperParsingException[mapping [type-name]]; nested: MapperParsingException[No type specified for property [field-name]]; \"\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    static String indexSuccessResult = "{\n" +
            "    \"took\": 36,\n" +
            "    \"errors\": false,\n" +
            "    \"items\": [\n" +
            "        {\n" +
            "            \"index\": {\n" +
            "                \"_index\": \"foo\",\n" +
            "                \"_type\": \"FooBar\",\n" +
            "                \"_id\": \"12345\",\n" +
            "                \"status\": 201,\n" +
            "                \"_version\": 3,\n" +
            "                \"_shards\": {\n" +
            "                    \"total\": 1,\n" +
            "                    \"successful\": 1,\n" +
            "                    \"failed\": 0\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    ]\n" +
            "}";

  static String indexFailedResultObject = "{\n" +
      "    \"took\": 10,\n" +
      "    \"errors\": true,\n" +
      "    \"items\": [\n" +
      "        {\n" +
      "            \"index\": {\n" +
      "                \"_index\": \"index-name\",\n" +
      "                \"_type\": \"type-name\",\n" +
      "                \"_id\": null,\n" +
      "                \"status\": 400,\n" +
      "                \"error\": {\n" +
      "                    \"type\": \"type_missing_exception\",\n" +
      "                    \"reason\": \"Reason is missing type\",\n" +
      "                    \"index\": \"foo\"\n" +
      "                }\n" +
      "            }\n" +
      "        }\n" +
      "    ]\n" +
      "}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Test
    public void bulkResultWithFailures() throws IOException {
        BulkResult bulkResult = new BulkResult(new ObjectMapper());
        bulkResult.setJsonString(indexFailedResult);
        bulkResult.setJsonMap(objectMapper.readValue(indexFailedResult, Map.class));
        bulkResult.setSucceeded(false);

        assertEquals(1, bulkResult.getItems().size());
        assertEquals(1, bulkResult.getFailedItems().size());
        assertNull(bulkResult.getItems().get(0).version);

        assertEquals(
            "\"MapperParsingException[mapping [type-name]]; nested: MapperParsingException[No type specified for property [field-name]]; \"",
            bulkResult.getItems().get(0).error);
        assertNull(bulkResult.getItems().get(0).errorType);
        assertNull(bulkResult.getItems().get(0).errorReason);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void bulkResultWithSuccess() throws IOException {
        BulkResult bulkResult = new BulkResult(new ObjectMapper());
        bulkResult.setJsonString(indexSuccessResult);
        bulkResult.setJsonMap(objectMapper.readValue(indexSuccessResult, Map.class));
        bulkResult.setSucceeded(true);

        assertEquals(1, bulkResult.getItems().size());
        assertEquals(0, bulkResult.getFailedItems().size());
        assertEquals(201, bulkResult.getItems().get(0).status);
        assertEquals(Integer.valueOf(3), bulkResult.getItems().get(0).version);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void bulkResultWithFailuresObject() throws IOException {
        BulkResult bulkResult = new BulkResult(new ObjectMapper());
        bulkResult.setJsonString(indexFailedResultObject);
        bulkResult.setJsonMap(objectMapper.readValue(indexFailedResultObject, Map.class));
        bulkResult.setSucceeded(false);

        assertEquals(1, bulkResult.getItems().size());
        assertEquals(1, bulkResult.getFailedItems().size());
        assertNull(bulkResult.getItems().get(0).version);

        assertEquals("type_missing_exception", bulkResult.getItems().get(0).errorType);
        assertEquals("Reason is missing type", bulkResult.getItems().get(0).errorReason);
    }
}
