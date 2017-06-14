package io.searchbox.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestResult;
import io.searchbox.common.AbstractIntegrationTest;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.io.IOException;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE, numDataNodes = 1)
public class StatsIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void clusterStats() throws IOException {
        JestResult result = client.execute(new Stats.Builder().build());
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        JsonNode resultJson = result.getJsonObject();
        assertNotNull(resultJson);
        assertNotNull(resultJson.get("timestamp"));
        assertNotNull(resultJson.get("cluster_name"));
        assertNotNull(resultJson.get("status"));
        assertNotNull(resultJson.get("indices"));
        assertNotNull(resultJson.get("nodes"));
        assertEquals(internalCluster().size(), resultJson.path("nodes").path("count").path("total").intValue());

    }

    @Test
    public void clusterStatsWithSpecificNodes() throws IOException {
        final String localNodeName = clusterService().localNode().name();
        JestResult result = client.execute(new Stats.Builder().addNode(localNodeName).build());
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        JsonNode resultJson = result.getJsonObject();
        assertNotNull(resultJson);
        assertNotNull(resultJson.get("timestamp"));
        assertNotNull(resultJson.get("cluster_name"));
        assertNotNull(resultJson.get("indices"));
        assertNotNull(resultJson.get("nodes"));
        assertEquals(1, resultJson.path("nodes").path("count").path("total").intValue());
    }
}
