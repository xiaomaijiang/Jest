package io.searchbox.indices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Dogukan Sonmez
 */
public class CreateIndexTest {
    @Test
    public void createIndexWithoutSettings() throws IOException {
        CreateIndex createIndex = new CreateIndex.Builder("tweet").build();

        assertEquals("tweet", createIndex.getURI());
        assertEquals("PUT", createIndex.getRestMethodName());
        final ObjectMapper objectMapper = new ObjectMapper();
        String settings = objectMapper.writeValueAsString(createIndex.getData(objectMapper));
        assertEquals("\"{}\"", settings);
    }

    @Test
    public void equalsReturnsTrueForSameSettings() {
        final Settings.Builder indexerSettings = Settings.settingsBuilder();
        indexerSettings.put("analysis.analyzer.events.type", "custom");
        indexerSettings.put("analysis.analyzer.events.tokenizer", "standard");
        indexerSettings.put("analysis.analyzer.events.filter", "snowball, standard, lowercase");
        CreateIndex createIndex1 = new CreateIndex.Builder("tweet").settings(indexerSettings.build().getAsMap()).build();
        CreateIndex createIndex1Duplicate = new CreateIndex.Builder("tweet").settings(indexerSettings.build().getAsMap()).build();

        assertEquals(createIndex1, createIndex1Duplicate);
    }

    @Test
    public void equalsReturnsFalseForDifferentSettings() {
        final Settings.Builder indexerSettings = Settings.settingsBuilder();
        indexerSettings.put("analysis.analyzer.events.type", "custom");
        indexerSettings.put("analysis.analyzer.events.tokenizer", "standard");
        CreateIndex createIndex1 = new CreateIndex.Builder("tweet").settings(indexerSettings.build().getAsMap()).build();
        indexerSettings.put("analysis.analyzer.events.filter", "snowball, standard, lowercase");
        CreateIndex createIndex2 = new CreateIndex.Builder("tweet").settings(indexerSettings.build().getAsMap()).build();

        assertNotEquals(createIndex1, createIndex2);
    }

}
