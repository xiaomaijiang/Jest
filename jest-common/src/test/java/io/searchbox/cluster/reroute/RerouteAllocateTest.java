package io.searchbox.cluster.reroute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

public class RerouteAllocateTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void allowPrimaryTrue() throws JSONException, JsonProcessingException {
        RerouteAllocate allocateReplica = new RerouteAllocate("index1", 1, "node1", true);

        assertEquals(allocateReplica.getType(), "allocate");

        String actualJson = objectMapper.writeValueAsString(allocateReplica.getData());
        String expectedJson = "{\"index\":\"index1\", \"shard\": 1, \"node\": \"node1\", \"allow_primary\": true}";
        JSONAssert.assertEquals(actualJson, expectedJson, false);
    }

    @Test
    public void allowPrimaryFalse() throws JSONException, JsonProcessingException {
        RerouteAllocate allocateReplica = new RerouteAllocate("index1", 1, "node1", false);

        assertEquals(allocateReplica.getType(), "allocate");

        String actualJson = objectMapper.writeValueAsString(allocateReplica.getData());
        String expectedJson = "{\"index\":\"index1\", \"shard\": 1, \"node\": \"node1\", \"allow_primary\": false}";
        JSONAssert.assertEquals(actualJson, expectedJson, false);
    }

}