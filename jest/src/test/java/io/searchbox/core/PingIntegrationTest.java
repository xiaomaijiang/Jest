package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestResult;
import io.searchbox.common.AbstractIntegrationTest;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.io.IOException;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE, numDataNodes = 1)
public class PingIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void simplePing() throws IOException {
        Ping ping = new Ping.Builder().build();
        JestResult result = client.execute(ping);

        assertTrue(result.getErrorMessage(), result.isSucceeded());
        final JsonNode responseJson = result.getJsonObject();
        assertNotNull(responseJson.get("name"));
        assertNotNull(responseJson.get("cluster_name"));
        assertNotNull(responseJson.get("cluster_uuid"));
        assertNotNull(responseJson.get("version"));
        assertEquals("You Know, for Search", responseJson.get("tagline").asText());
    }
}
