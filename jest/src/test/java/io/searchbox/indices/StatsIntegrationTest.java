package io.searchbox.indices;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestResult;
import io.searchbox.common.AbstractIntegrationTest;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author cihat keser
 */
@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE, numDataNodes = 1)
public class StatsIntegrationTest extends AbstractIntegrationTest {

    private static final String INDEX_NAME = "flush_test_index";
    private static final String STATS_WITH_OPTIONS_INDEX_NAME = "stats_with_options_index";

    @Test
    public void testDefaultStats() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        createIndex(INDEX_NAME);
        ensureSearchable(INDEX_NAME);

        Stats stats = new Stats.Builder().build();
        JestResult result = client.execute(stats);
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        // confirm that response has all the default stats types
        JsonNode jsonResult = result.getJsonObject();
        JsonNode statsJson = jsonResult.get("indices").get(INDEX_NAME).get("total");
        assertNotNull(statsJson);
        assertNotNull(statsJson.get("docs"));
        assertNotNull(statsJson.get("store"));
        assertNotNull(statsJson.get("indexing"));
        assertNotNull(statsJson.get("get"));
        assertNotNull(statsJson.get("search"));
    }

    @Test
    public void testStatsWithOptions() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        createIndex(STATS_WITH_OPTIONS_INDEX_NAME);
        ensureSearchable(STATS_WITH_OPTIONS_INDEX_NAME);

        Stats stats = new Stats.Builder()
                .flush(true)
                .indexing(true)
                .build();

        JestResult result = client.execute(stats);
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        // Confirm that response has only flush and indexing stats types
        JsonNode jsonResult = result.getJsonObject();
        JsonNode statsJson = jsonResult.get("indices").get(STATS_WITH_OPTIONS_INDEX_NAME).get("total");
        assertNotNull(statsJson);
        assertNotNull(statsJson.get("flush"));
        assertNotNull(statsJson.get("indexing"));
        assertEquals("Number of stats received", 2, statsJson.size());
    }

}
