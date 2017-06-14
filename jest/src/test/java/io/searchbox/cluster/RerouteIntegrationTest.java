package io.searchbox.cluster;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.reroute.RerouteAllocate;
import io.searchbox.cluster.reroute.RerouteCancel;
import io.searchbox.cluster.reroute.RerouteCommand;
import io.searchbox.cluster.reroute.RerouteMove;
import io.searchbox.common.AbstractIntegrationTest;
import io.searchbox.core.Cat;
import io.searchbox.core.CatResult;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 3)
public class RerouteIntegrationTest extends AbstractIntegrationTest {

    static final String INDEX = "reroute";

    @Before
    public void beforeTest() throws IOException {
        createIndex(INDEX);
        ensureSearchable(INDEX);
        setAllocationDisabled(true);
    }

    @After
    public void afterTest() throws IOException {
        setAllocationDisabled(false);
    }

    @Test
    public void move() throws IOException, InterruptedException {
        int shardToReroute = 0;

        String fromNode = getNodeOfPrimaryShard(INDEX, shardToReroute);
        String toNode = getAvailableNodeForShard(INDEX, shardToReroute);

        RerouteMove rerouteMove = new RerouteMove(INDEX, shardToReroute, fromNode, toNode);
        JestResult result = client.execute(new Reroute.Builder(rerouteMove).build());
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        waitUntilPrimaryShardInNode(shardToReroute, toNode);
    }

    @Test
    public void cancelAndAllocate() throws IOException, InterruptedException {
        int shardToReroute = 0;

        String fromNode = getNodeOfPrimaryShard(INDEX, shardToReroute);
        String toNode = getAvailableNodeForShard(INDEX, shardToReroute);

        List<RerouteCommand> commands = ImmutableList.of(
                new RerouteCancel(INDEX, shardToReroute, fromNode, true),
                new RerouteAllocate(INDEX, shardToReroute, toNode, true)
        );
        JestResult result = client.execute(new Reroute.Builder(commands).build());
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        waitUntilPrimaryShardInNode(shardToReroute, toNode);
    }

    private void setAllocationDisabled(boolean disabled) throws IOException {
        String source = "{\n" +
                "    \"transient\" : {\n" +
                "        \"cluster.routing.allocation.disable_allocation\": \"" + disabled + "\" " +
                "    }\n" +
                "}";

        UpdateSettings updateSettings = new UpdateSettings.Builder(source).build();
        JestResult result = client.execute(updateSettings);
        assertTrue(result.getErrorMessage(), result.isSucceeded());
    }

    private String getAvailableNodeForShard(String index, int shard) throws IOException {
        Set<String> dataNodes = getAllDataNodes();

        String primaryShardNode = getNodeOfPrimaryShard(index, shard);
        Set<String> replicaShardNodes = getNodesOfReplicaShard(index, shard);
        dataNodes.remove(primaryShardNode);
        dataNodes.removeAll(replicaShardNodes);

        if (dataNodes.size() < 1) {
            throw new RuntimeException("No Available node for shard=" + shard + " index=" + index);
        }

        return dataNodes.iterator().next();
    }

    private Set<String> getAllDataNodes() throws IOException {
        CatResult result = client.execute(new Cat.NodesBuilder().build());
        JsonNode nodes = result.getJsonObject().get("result");

        Set<String> nodeNames = new HashSet<String>();
        for (JsonNode nodeElement : nodes) {
            JsonNode nodeObj = nodeElement;
            String nodeRole = nodeObj.get("node.role").asText();
            if (nodeRole.indexOf('d') >= 0) {
                nodeNames.add(nodeObj.get("name").asText());
            }
        }

        return nodeNames;
    }

    private String getNodeOfPrimaryShard(String index, int shard) throws IOException {
        Set<String> nodeOfShard = getNodeOfShard(index, shard, true);
        if (nodeOfShard.size() == 0) {
            return null;
        }
        return nodeOfShard.iterator().next();
    }

    private Set<String> getNodesOfReplicaShard(String index, int shard) throws IOException {
        return getNodeOfShard(index, shard, false);
    }

    private Set<String> getNodeOfShard(String index, int shard, boolean isPrimary) throws IOException {
        String prirep = isPrimary ? "p" : "r";
        CatResult result = client.execute(new Cat.ShardsBuilder().addIndex(index).setParameter("h", "shard,node,prirep").build());
        JsonNode shards = result.getJsonObject().get("result");

        Set<String> resultSet = new HashSet<>();
        for (JsonNode shardElement : shards) {
            if (shardElement.get("shard").asInt() == shard && prirep.equals(shardElement.get("prirep").asText())) {
                resultSet.add(shardElement.get("node").asText());
            }
        }

        return resultSet;
    }

    private void waitUntilPrimaryShardInNode(int shard, String expectedNode) throws InterruptedException, IOException {
        int retries = 0;
        int maxAttempts = 3;
        String currentNode = getNodeOfPrimaryShard(INDEX, shard);
        while (retries < 3 && currentNode != null && !currentNode.equals(expectedNode)) {
            retries++;
            currentNode = getNodeOfPrimaryShard(INDEX, shard);
            Thread.sleep(1000);
        }
        if (retries >= maxAttempts) {
            fail("Primary shard " + shard + " expected to be in node " + expectedNode + " but is in " + currentNode);
        }
    }

}