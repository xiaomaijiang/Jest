package io.searchbox.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author cihat keser
 */
public class CountResultTest {

    String json = "{\n" +
            "    \"count\" : 1,\n" +
            "    \"_shards\" : {\n" +
            "        \"total\" : 5,\n" +
            "        \"successful\" : 5,\n" +
            "        \"failed\" : 0\n" +
            "    }\n" +
            "}";

    @Test
    public void testGetCount() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        CountResult countResult = new CountResult(objectMapper);
        countResult.setSucceeded(true);
        countResult.setJsonString(json);
        countResult.setJsonObject(objectMapper.readTree(json));
        countResult.setPathToResult("count");

        Double count = countResult.getCount();
        assertNotNull(count);
    }

    @Test
    public void testGetCountWhenOperationFails() {
        CountResult countResult = new CountResult(new ObjectMapper());
        countResult.setSucceeded(false);

        Double count = countResult.getCount();
        assertNull(count);
    }

}
