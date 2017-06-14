package io.searchbox.cluster.reroute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

public class RerouteMoveTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void move() throws JSONException, JsonProcessingException {
        RerouteMove rerouteMove = new RerouteMove("index1", 1, "node1", "node2");

        assertEquals(rerouteMove.getType(), "move");

        String actualJson = objectMapper.writeValueAsString(rerouteMove.getData());
        String expectedJson = "{\"index\":\"index1\", \"shard\": 1, \"from_node\": \"node1\", \"to_node\": \"node2\"}";
        JSONAssert.assertEquals(actualJson, expectedJson, false);
    }

}