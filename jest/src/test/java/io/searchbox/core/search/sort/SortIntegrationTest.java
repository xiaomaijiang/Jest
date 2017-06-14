package io.searchbox.core.search.sort;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestResult;
import io.searchbox.common.AbstractIntegrationTest;
import io.searchbox.core.Search;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author ferhat
 * @author cihat keser
 */
@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST, numDataNodes = 1)
public class SortIntegrationTest extends AbstractIntegrationTest {

    String query = "{\"query\":{ \"match_all\" : { }}}";
    String index = "ranker";
    String type = "ranking";

    @Before
    public void setup() {
        createIndex(index);
        client().admin().indices().putMapping(new PutMappingRequest(index)
                        .type(type)
                        .source("{\"ranking\":{\"properties\":{\"rank\":{\"store\":true,\"type\":\"integer\"}}}}")
        ).actionGet();

        client().index(new IndexRequest(index, type).source("{\"rank\":10}").refresh(true)).actionGet();
        client().index(new IndexRequest(index, type).source("{\"rank\":5}").refresh(true)).actionGet();
        client().index(new IndexRequest(index, type).source("{\"rank\":8}").refresh(true)).actionGet();

        ensureSearchable(index);
    }

    @Test
    public void searchWithSimpleFieldSort() throws IOException {
        Sort sort = new Sort("rank");
        Search search = new Search.Builder(query)
                .addSort(sort)
                .addIndex(index)
                .addType(type)
                .build();
        JestResult result = client.execute(search);
        assertTrue(result.getErrorMessage(), result.isSucceeded());
        JsonNode hits = result.getJsonObject().path("hits").path("hits");
        assertEquals(3, hits.size());
        assertEquals(5D, hits.path(0).path("_source").path("rank").asDouble(), 0.0D);
        assertEquals(8D, hits.path(1).path("_source").path("rank").asDouble(), 0.0D);
        assertEquals(10D, hits.path(2).path("_source").path("rank").asDouble(), 0.0D);
    }

    @Test
    public void searchWithCustomSort() throws IOException {
        Sort sort = new Sort("rank", Sort.Sorting.DESC);
        Search search = new Search.Builder(query)
                .addSort(sort)
                .addIndex(index)
                .addType(type)
                .build();
        JestResult result = client.execute(search);
        assertTrue(result.getErrorMessage(), result.isSucceeded());
        JsonNode hits = result.getJsonObject().path("hits").path("hits");
        assertEquals(3, hits.size());
        assertEquals(5D, hits.path(2).path("_source").path("rank").asDouble(), 0D);
        assertEquals(8D, hits.path(1).path("_source").path("rank").asDouble(), 0D);
        assertEquals(10D, hits.path(0).path("_source").path("rank").asDouble(), 0D);
    }

}
