package io.searchbox.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.annotations.JestId;
import io.searchbox.annotations.JestVersion;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author cihat keser
 */
public class SearchResultTest {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    String json = "{\n" +
            "    \"_shards\":{\n" +
            "        \"total\" : 5,\n" +
            "        \"successful\" : 5,\n" +
            "        \"failed\" : 0\n" +
            "    },\n" +
            "    \"hits\":{\n" +
            "        \"total\" : 1,\n" +
            "        \"hits\" : [\n" +
            "            {\n" +
            "                \"_index\" : \"twitter\",\n" +
            "                \"_type\" : \"tweet\",\n" +
            "                \"_id\" : \"1\",\n" +
            "                \"_source\" : {\n" +
            "                    \"user\" : \"kimchy\",\n" +
            "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
            "                    \"message\" : \"trying out Elasticsearch\"\n" +
            "                },\n" +
            "                \"sort\" : [\n" +
            "                     1234.5678\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    @Test
    public void testGetMaxScoreWhenMissing() throws IOException {
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(json);
        searchResult.setJsonObject(objectMapper.readTree(json));
        searchResult.setPathToResult("hits/hits/_source");

        Float maxScore = searchResult.getMaxScore();
        assertNull(maxScore);
    }

    @Test
    public void testGetMaxScore() throws IOException {
        String jsonWithMaxScore = "{\n" +
                "    \"_shards\":{\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    },\n" +
                "    \"hits\":{\n" +
                "        \"max_score\" : 0.028130025,\n" +
                "        \"total\" : 1,\n" +
                "        \"hits\" : [\n" +
                "            {\n" +
                "                \"_index\" : \"twitter\",\n" +
                "                \"_type\" : \"tweet\",\n" +
                "                \"_id\" : \"1\",\n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elasticsearch\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(jsonWithMaxScore);
        searchResult.setJsonObject(objectMapper.readTree(jsonWithMaxScore));
        searchResult.setPathToResult("hits/hits/_source");

        Float maxScore = searchResult.getMaxScore();
        assertNotNull(maxScore);
        assertEquals(new Float("0.028130025"), maxScore);
    }

    @Test
    public void testGetTotal() throws IOException {
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(json);
        searchResult.setJsonObject(objectMapper.readTree(json));
        searchResult.setPathToResult("hits/hits/_source");

        Long total = searchResult.getTotal();
        assertNotNull(total);
        assertEquals(new Long(1L), total);
    }

    @Test
    public void testGetTotalWhenTotalMissing() throws IOException {
        String jsonWithoutTotal = "{\n" +
                "    \"_shards\":{\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    },\n" +
                "    \"hits\":{\n" +
                "        \"hits\" : [\n" +
                "            {\n" +
                "                \"_index\" : \"twitter\",\n" +
                "                \"_type\" : \"tweet\",\n" +
                "                \"_id\" : \"1\",\n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elasticsearch\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(jsonWithoutTotal);
        searchResult.setJsonObject(objectMapper.readTree(jsonWithoutTotal));
        searchResult.setPathToResult("hits/hits/_source");

        Long total = searchResult.getTotal();
        assertNull(total);
    }

    @Test
    public void testGetHits() throws IOException {
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(json);
        searchResult.setJsonObject(objectMapper.readTree(json));
        searchResult.setPathToResult("hits/hits/_source");

        List hits = searchResult.getHits(Object.class);
        assertNotNull(hits);
        assertFalse("should have 1 hit", hits.isEmpty());

        hits = searchResult.getHits(Object.class, Object.class);
        assertNotNull(hits);
        assertFalse("should have 1 hit", hits.isEmpty());
    }

    @Test
    public void testGetHitsWithoutMetadata() throws IOException {
        final SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(json);
        searchResult.setJsonObject(objectMapper.readTree(json));
        searchResult.setPathToResult("hits/hits/_source");

        assertTrue(getFirstHitSource(searchResult.getHits(Object.class)).containsKey(SearchResult.ES_METADATA_ID));
        assertFalse(getFirstHitSource(searchResult.getHits(Object.class, false)).containsKey(SearchResult.ES_METADATA_ID));
        assertTrue(getFirstHitSource(searchResult.getHits(Object.class, Object.class)).containsKey(SearchResult.ES_METADATA_ID));
        assertFalse(getFirstHitSource(searchResult.getHits(Object.class, Object.class, false)).containsKey(SearchResult.ES_METADATA_ID));
    }

    private Map getFirstHitSource(List hits) {
        assertNotNull(hits);
        assertTrue("should have 1 hit", hits.size() == 1);
        SearchResult.Hit hit = (SearchResult.Hit) hits.get(0);
        assertNotNull(hit.source);
        return (Map) hit.source;
    }

    @Test
    public void testGetFirstHit() throws IOException {
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(json);
        searchResult.setJsonObject(objectMapper.readTree(json));
        searchResult.setPathToResult("hits/hits/_source");

        SearchResult.Hit hit = searchResult.getFirstHit(Object.class);
        assertNotNull(hit);
        assertNotNull(hit.source);
        assertNull(hit.explanation);
        assertNotNull(hit.sort);
        assertNotNull(hit.id);
        assertNull(hit.score);

        hit = searchResult.getFirstHit(Object.class, Object.class);
        assertNotNull(hit);
        assertNotNull(hit.source);
        assertNull(hit.explanation);
        assertNotNull(hit.sort);
        assertNotNull(hit.id);
        assertNull(hit.score);
    }

    @Test
    public void testGetHitsWhenOperationFails() {
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(false);

        List hits = searchResult.getHits(Object.class);
        assertNotNull(hits);
        assertTrue(hits.isEmpty());

        hits = searchResult.getHits(Object.class, Object.class);
        assertNotNull(hits);
        assertTrue(hits.isEmpty());
    }

    @Test
    public void testGetFirstHitWhenOperationFails() {
        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(false);

        SearchResult.Hit hit = searchResult.getFirstHit(Object.class);
        assertNull(hit);

        hit = searchResult.getFirstHit(Object.class, Object.class);
        assertNull(hit);
    }

    @Test
    public void testGetScore() throws IOException {
        String jsonWithScore = "{\n" +
                "    \"_shards\":{\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    },\n" +
                "    \"hits\":{\n" +
                "        \"total\" : 1,\n" +
                "        \"hits\" : [\n" +
                "            {\n" +
                "                \"_index\" : \"twitter\",\n" +
                "                \"_type\" : \"tweet\",\n" +
                "                \"_score\" : \"1.02332\",\n" +
                "                \"_id\" : \"1\",\n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elasticsearch\"\n" +
                "                },\n" +
                "                \"sort\" : [\n" +
                "                     1234.5678\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(jsonWithScore);
        searchResult.setJsonObject(objectMapper.readTree(jsonWithScore));
        searchResult.setPathToResult("hits/hits/_source");

        SearchResult.Hit hit = searchResult.getFirstHit(Object.class);
        assertNotNull(hit);
        assertNotNull(hit.source);
        assertNull(hit.explanation);
        assertNotNull(hit.sort);
        assertNotNull(hit.id);
        assertNotNull(hit.score);

        hit = searchResult.getFirstHit(Object.class, Object.class);
        assertNotNull(hit);
        assertNotNull(hit.source);
        assertNull(hit.explanation);
        assertNotNull(hit.sort);
        assertNotNull(hit.id);
        assertNotNull(hit.score);
    }

    @Test
    public void testGetVersion() throws IOException {
        Long someVersion = Integer.MAX_VALUE + 10L;

        String jsonWithVersion = "{\n" +
                "    \"_shards\":{\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    },\n" +
                "    \"hits\":{\n" +
                "        \"total\" : 1,\n" +
                "        \"hits\" : [\n" +
                "            {\n" +
                "                \"_index\" : \"twitter\",\n" +
                "                \"_type\" : \"tweet\",\n" +
                "                \"_score\" : \"1.02332\",\n" +
                "                \"_id\" : \"1\",\n" +
                "                \"_version\" : \"" + someVersion + "\",\n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elasticsearch\"\n" +
                "                },\n" +
                "                \"sort\" : [\n" +
                "                     1234.5678\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        SearchResult searchResult = new SearchResult(objectMapper);
        searchResult.setSucceeded(true);
        searchResult.setJsonString(jsonWithVersion);
        searchResult.setJsonObject(objectMapper.readTree(jsonWithVersion));
        searchResult.setPathToResult("hits/hits/_source");

        SearchResult.Hit<TestObject, Void> hit = searchResult.getFirstHit(TestObject.class);
        assertNotNull(hit.source);
        assertNull(hit.explanation);
        assertNotNull(hit.sort);
        assertNotNull(hit.score);
        assertEquals("Incorrect version", someVersion, hit.source.getVersion());
    }

    private static class TestObject {
        @JestId
        private String id;

        @JestVersion
        private Long version;

        public TestObject() {}

        public Long getVersion() {
            return version;
        }

        public String getId() {
            return id;
        }
    }

}
