package io.searchbox.core.search.sort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.core.search.sort.Sort.Missing;
import io.searchbox.core.search.sort.Sort.Sorting;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author Riccardo Tasso
 * @author cihat keser
 */
public class SortTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testJsonSerializationForSimpleFieldSort() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{}}";
        Sort s = new Sort("my_field");
        JsonNode actualJsonObject = s.toJsonObject(objectMapper);

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationForAscOrder() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"order\":\"asc\"}}";
        Sort s = new Sort("my_field", Sort.Sorting.ASC);
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationForDescOrder() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"order\":\"desc\"}}";
        Sort s = new Sort("my_field", Sorting.DESC);
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationForMissingValueFirst() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"missing\":\"_first\"}}";
        Sort s = new Sort("my_field");
        s.setMissing(Missing.FIRST);
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationForMissingValueLast() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"missing\":\"_last\"}}";
        Sort s = new Sort("my_field");
        s.setMissing(Missing.LAST);
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationForMissingValueString() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"missing\":\"***\"}}";
        Sort s = new Sort("my_field");
        s.setMissing("***");
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationForMissingValueInteger() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"missing\":\"-1\"}}";
        Sort s = new Sort("my_field");
        s.setMissing(-1);
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationWithOrderAndMissingValue() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"order\":\"desc\",\"missing\":\"-1\"}}";
        Sort s = new Sort("my_field", Sorting.DESC);
        s.setMissing(-1);
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

    @Test
    public void testJsonSerializationWithUnmappedValue() throws JSONException, JsonProcessingException {
        String expectedJson = "{\"my_field\":{\"ignore_unmapped\":true}}";
        Sort s = new Sort("my_field");
        s.setIgnoreUnmapped();
        JsonNode actualJsonObject = s.toJsonObject(new ObjectMapper());

        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(actualJsonObject), false);
    }

}
