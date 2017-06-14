package io.searchbox.indices.settings;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestResult;
import io.searchbox.common.AbstractIntegrationTest;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.io.IOException;

/**
 * @author cihat keser
 */
@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE, numDataNodes = 1)
public class GetSettingsIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void testBasicFlow() throws IOException {
        String index = "test";

        createIndex(index);
        ensureGreen(index);

        GetSettings getSettings = new GetSettings.Builder().build();
        JestResult result = client.execute(getSettings);
        assertTrue(result.getErrorMessage(), result.isSucceeded());

        assertTrue(result.isSucceeded());
        System.out.println("result.getJsonString() = " + result.getJsonString());
        JsonNode json = result.getJsonObject();
        assertNotNull(json.get(index));
        assertNotNull(json.get(index).get("settings"));
    }

    @Test
    public void testForNonexistentIndex() throws IOException {
        String index = "test";

        createIndex(index);
        ensureGreen(index);

        GetSettings getSettings = new GetSettings.Builder().addIndex("nonExisting").build();
        JestResult result = client.execute(getSettings);
        assertFalse(result.isSucceeded());
    }

}