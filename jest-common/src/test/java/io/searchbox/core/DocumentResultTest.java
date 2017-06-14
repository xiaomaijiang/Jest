package io.searchbox.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Bartosz Polnik
 */
public class DocumentResultTest {
    static DocumentResult validResult = new DocumentResult(new ObjectMapper());
    static DocumentResult invalidResult = new DocumentResult(new ObjectMapper());
    static String validResponse = "{\n" +
            "    \"_index\": \"testIndex\",\n" +
            "    \"_type\": \"testType\",\n" +
            "    \"_id\": \"testId\",\n" +
            "    \"_version\": 2\n" +
            "}";

    static String invalidResponse = "{\n" +
            "    \"error\": \"NullPointerException[null]\",\n" +
            "    \"status\": 500\n" +
            "}";

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setUp() throws IOException {
        validResult.setJsonMap(new ObjectMapper().readValue(validResponse, Map.class));
        invalidResult.setJsonMap(new ObjectMapper().readValue(invalidResponse, Map.class));
    }

    @Test
    public void shouldFetchIndexFromValidResponse() {
        assertEquals("testIndex", validResult.getIndex());
    }

    @Test
    public void shouldFetchTypeFromValidResponse() {
        assertEquals("testType", validResult.getType());
    }

    @Test
    public void shouldFetchIdFromValidResponse() {
        assertEquals("testId", validResult.getId());
    }

    @Test
    public void shouldFetchVersionFromValidResponse() {
        assertEquals(Long.valueOf(2), validResult.getVersion());
    }

    @Test
    public void shouldReturnNullIndexOnInvalidResponse() {
        assertNull(invalidResult.getIndex());
    }
}