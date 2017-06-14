package io.searchbox.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.action.Action;
import io.searchbox.core.search.sort.Sort;
import io.searchbox.core.search.sort.Sort.Sorting;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dogukan Sonmez
 * @author Riccardo Tasso
 * @author cihat keser
 */
public class SearchTest {

    Sort sortByPopulationAsc = new Sort("population", Sorting.ASC);
    Sort sortByPopulationDesc = new Sort("population", Sorting.DESC);
    Sort sortByPopulation = new Sort("population");

    @Test
    public void getURIWithoutIndexAndType() {
        Action search = new Search.Builder("").build();
        assertEquals("_all/_search", search.getURI());
    }

    @Test
    public void getURIWithOnlyOneIndex() {
        Action search = new Search.Builder("").addIndex("twitter").build();
        assertEquals("twitter/_search", search.getURI());
    }

    @Test
    public void getURIWithOneIndexAndOneType() {
        Action search = new Search.Builder("").addIndex("twitter").addType("tweet").build();
        assertEquals("twitter/tweet/_search", search.getURI());
    }

    @Test
    public void getURIWithOnlyOneType() {
        Action search = new Search.Builder("").addType("tweet").build();
        assertEquals("_all/tweet/_search", search.getURI());
    }

    @Test
    public void getURIWithOnlyMultipleIndex() {
        Action search = new Search.Builder("").addIndex("twitter").addIndex("searchbox").build();
        assertEquals("twitter%2Csearchbox/_search", search.getURI());
    }

    @Test
    public void getURIWithOnlyMultipleType() {
        Action search = new Search.Builder("").addType("tweet").addType("jest").build();
        assertEquals("_all/tweet%2Cjest/_search", search.getURI());
    }

    @Test
    public void getURIWithMultipleIndexAndTypes() {
        Action search = new Search.Builder("")
                .addIndex("twitter")
                .addIndex("searchbox")
                .addType("tweet")
                .addType("jest")
                .build();
        assertEquals("twitter%2Csearchbox/tweet%2Cjest/_search", search.getURI());
    }

    @Test
    public void getURIForTemplateWithoutIndexAndType() {
        Action search = new Search.TemplateBuilder("").build();
        assertEquals("_all/_search/template", search.getURI());
    }

    @Test
    public void getURIForTemplateWithIndexAndType() {
        Action search = new Search.TemplateBuilder("").addIndex("twitter").addType("tweet").build();
        assertEquals("twitter/tweet/_search/template", search.getURI());
    }

    @Test
    public void getURIWithVersion() {
        Action search = new Search.VersionBuilder("").addIndex("twitter").addType("tweet").build();
        assertTrue("Version Parameter missing", search.getURI().contains("version=true"));
    }

    @Test
    public void sourceFilteringByQueryTest() throws IOException {
        String query = "{\"sort\":[],\"_source\":{\"exclude\":[\"excludeFieldName\"],\"include\":[\"includeFieldName\"]}}";
        Action search = new Search.Builder(query).build();

        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsed = objectMapper.readTree(search.getData(objectMapper));
        JsonNode obj = parsed;
        JsonNode source = obj.get("_source");

        JsonNode includePattern = source.get("include");
        assertEquals(1, includePattern.size());
        assertEquals("includeFieldName", includePattern.get(0).asText());

        JsonNode excludePattern = source.get("exclude");
        assertEquals(1, excludePattern.size());
        assertEquals("excludeFieldName", excludePattern.get(0).asText());
    }

    @Test
    public void sourceFilteringParamTest() throws IOException {
        String query = "{\"query\" : { \"term\" : { \"name\" : \"KangSungJeon\" } }}";
        String includePatternItem1 = "SeolaIncludeFieldName";
        String includePatternItem2 = "SeohooIncludeFieldName";
        String excludePatternItem1 = "SeolaExcludeField.*";
        String excludePatternItem2 = "SeohooExcludeField.*";

        Action search = new Search.Builder(query)
                .addSourceIncludePattern(includePatternItem1)
                .addSourceIncludePattern(includePatternItem2)
                .addSourceExcludePattern(excludePatternItem1)
                .addSourceExcludePattern(excludePatternItem2)
                .build();

        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsed = objectMapper.readTree(search.getData(objectMapper));
        JsonNode obj = parsed;
        JsonNode source = obj.get("_source");

        JsonNode includePattern = source.get("include");
        assertEquals(2, includePattern.size());
        assertEquals(includePatternItem1, includePattern.get(0).asText());
        assertEquals(includePatternItem2, includePattern.get(1).asText());

        JsonNode excludePattern = source.get("exclude");
        assertEquals(2, excludePattern.size());
        assertEquals(excludePatternItem1, excludePattern.get(0).asText());
        assertEquals(excludePatternItem2, excludePattern.get(1).asText());
    }

    @Test
    public void supportElasticsearchPermissiveSourceFilteringSyntax() throws IOException {
        String query = "{\"query\" : { \"term\" : { \"name\" : \"KangSungJeon\" } }, \"_source\": false}";
        String includePatternItem1 = "SeolaIncludeFieldName";
        String excludePatternItem1 = "SeolaExcludeField.*";

        Action search = new Search.Builder(query)
                .addSourceIncludePattern(includePatternItem1)
                .addSourceExcludePattern(excludePatternItem1)
                .build();

        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsed = objectMapper.readTree(search.getData(objectMapper));
        JsonNode obj = parsed;
        JsonNode source = obj.get("_source");

        JsonNode includePattern = source.get("include");
        assertEquals(1, includePattern.size());
        assertEquals(includePatternItem1, includePattern.get(0).asText());

        JsonNode excludePattern = source.get("exclude");
        assertEquals(1, excludePattern.size());
        assertEquals(excludePatternItem1, excludePattern.get(0).asText());

        query = "{\"query\" : { \"term\" : { \"name\" : \"KangSungJeon\" } }, \"_source\": [\"includeFieldName1\", \"includeFieldName2\"]}";

        search = new Search.Builder(query)
                .addSourceIncludePattern(includePatternItem1)
                .addSourceExcludePattern(excludePatternItem1)
                .build();

        parsed = objectMapper.readTree(search.getData(objectMapper));
        obj = parsed;
        source = obj.get("_source");

        includePattern = source.get("include");
        assertEquals(3, includePattern.size());
        assertEquals("includeFieldName1", includePattern.get(0).asText());
        assertEquals("includeFieldName2", includePattern.get(1).asText());
        assertEquals(includePatternItem1, includePattern.get(2).asText());

        excludePattern = source.get("exclude");
        assertEquals(1, excludePattern.size());
        assertEquals(excludePatternItem1, excludePattern.get(0).asText());
    }

    @Test
    public void sortTest() throws IOException {
        String query = "{\"query\" : { \"term\" : { \"name\" : \"Milano\" } }}";
        Action search = new Search.Builder(query)
                .addSort(Arrays.asList(sortByPopulationAsc, sortByPopulationDesc, sortByPopulation)).build();

        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsed = objectMapper.readTree(search.getData(objectMapper));
        JsonNode obj = parsed;
        JsonNode sort = obj.get("sort");

        assertEquals(3, sort.size());

        // sort 0
        JsonNode test = sort.get(0);
        assertTrue(test.has("population"));

        test = test.get("population");
        assertTrue(test.has("order"));
        assertEquals("asc", test.get("order").asText());

        // sort 1
        test = sort.get(1);
        assertTrue(test.has("population"));

        test = test.get("population");
        assertTrue(test.has("order"));
        assertEquals("desc", test.get("order").asText());

        // sort 2
        test = sort.get(2);
        assertTrue(test.has("population"));

        test = test.get("population");
        assertFalse(test.has("order"));
        assertFalse(test.has("order"));
    }

    @Test
    public void addSortShouldNotOverrideExistingSortDefinitions() throws JSONException, IOException {
        JsonNode sortClause = buildSortClause(
                "{\"query\" : { \"term\" : { \"name\" : \"Milano\" } }, \"sort\": [{\"existing\": { \"order\": \"desc\" }}]}",
                Arrays.asList(sortByPopulationAsc, sortByPopulationDesc)
        );

        assertNotNull(sortClause);
        assertEquals(3, sortClause.size());

        JSONAssert.assertEquals("{\"existing\":{\"order\":\"desc\"}}", sortClause.get(0).toString(), false);
        JSONAssert.assertEquals("{\"population\":{\"order\":\"asc\"}}", sortClause.get(1).toString(), false);
        JSONAssert.assertEquals("{\"population\":{\"order\":\"desc\"}}", sortClause.get(2).toString(), false);
    }

    @Test
    public void supportElasticsearchPermissiveSortSyntax() throws IOException, JSONException {
        JsonNode sortClause = buildSortClause(
                "{\"query\" : { \"term\" : { \"name\" : \"Milano\" } }, \"sort\": \"existing\"}",
                Arrays.asList(sortByPopulationAsc)
        );

        assertNotNull(sortClause);
        assertEquals(2, sortClause.size());

        assertEquals("{\"existing\":{\"order\":\"asc\"}}", sortClause.get(0).toString());
        assertEquals("{\"population\":{\"order\":\"asc\"}}", sortClause.get(1).toString());

        sortClause = buildSortClause(
                "{\"query\" : { \"term\" : { \"name\" : \"Milano\" } }, \"sort\": \"_score\"}",
                Arrays.asList(sortByPopulationAsc)
        );

        assertNotNull(sortClause);
        assertEquals(2, sortClause.size());

        assertEquals("{\"_score\":{\"order\":\"desc\"}}", sortClause.get(0).toString());
        assertEquals("{\"population\":{\"order\":\"asc\"}}", sortClause.get(1).toString());

        sortClause = buildSortClause(
                "{\"query\" : { \"term\" : { \"name\" : \"Milano\" } }, \"sort\": { \"existing\": {\"order\":\"desc\"} }}",
                Arrays.asList(sortByPopulationAsc)
        );

        assertNotNull(sortClause);
        assertEquals(2, sortClause.size());

        JSONAssert.assertEquals("{\"existing\":{\"order\":\"desc\"}}", sortClause.get(0).toString(), false);
        JSONAssert.assertEquals("{\"population\":{\"order\":\"asc\"}}", sortClause.get(1).toString(), false);
    }

    @Test
    public void equalsReturnsTrueForSameQueries() {
        Search search1 = new Search.Builder("query1").addIndex("twitter").addType("tweet").build();
        Search search1Duplicate = new Search.Builder("query1").addIndex("twitter").addType("tweet").build();

        assertEquals(search1, search1Duplicate);
    }

    @Test
    public void equalsReturnsFalseForDifferentQueries() {
        Search search1 = new Search.Builder("query1").addIndex("twitter").addType("tweet").build();
        Search search2 = new Search.Builder("query2").addIndex("twitter").addType("tweet").build();

        assertNotEquals(search1, search2);
    }

    @Test
    public void equalsReturnsTrueForSameSortList() {
        Search search1 = new Search.Builder("query1").addIndex("twitter").addType("tweet")
                .addSort(Arrays.asList(sortByPopulationAsc, sortByPopulation)).build();
        Search search1Duplicate = new Search.Builder("query1").addIndex("twitter").addType("tweet")
                .addSort(Arrays.asList(sortByPopulationAsc, sortByPopulation)).build();

        assertEquals(search1, search1Duplicate);
    }

    @Test
    public void equalsReturnsFalseForDifferentSortList() {
        Search search1 = new Search.Builder("query1").addIndex("twitter").addType("tweet")
                .addSort(sortByPopulationAsc).build();
        Search search1Duplicate = new Search.Builder("query1").addIndex("twitter").addType("tweet")
                .addSort(sortByPopulationDesc).build();

        assertNotEquals(search1, search1Duplicate);
    }

    private JsonNode buildSortClause(String query, List<Sort> sorts) throws IOException {
        Action search = new Search.Builder(query).addSort(sorts).build();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsed = objectMapper.readTree(search.getData(objectMapper));

        return parsed.get("sort");
    }
}
