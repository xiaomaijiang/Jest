package io.searchbox.indices.aliases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.collect.MapBuilder;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

/**
 * @author cihat keser
 */
public class AddAliasMappingTest {

    public static final Map<String, Object> USER_FILTER_JSON = new MapBuilder<String, Object>()
            .put("term", MapBuilder.newMapBuilder()
                    .put("user", "kimchy")
                    .immutableMap())
            .immutableMap();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testBasicGetDataForJson() throws JSONException, JsonProcessingException {
        AddAliasMapping addAliasMapping = new AddAliasMapping
                .Builder("tIndex", "tAlias")
                .build();
        String actualJson = objectMapper.writeValueAsString(addAliasMapping.getData());
        String expectedJson = "[{\"add\":{\"index\":\"tIndex\",\"alias\":\"tAlias\"}}]";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void testGetDataForJsonWithFilter() throws JSONException, JsonProcessingException {
        AddAliasMapping addAliasMapping = new AddAliasMapping
                .Builder("tIndex", "tAlias")
                .setFilter(USER_FILTER_JSON)
                .build();
        String actualJson = objectMapper.writeValueAsString(addAliasMapping.getData());
        String expectedJson = "[{\"add\":{\"index\":\"tIndex\",\"alias\":\"tAlias\",\"filter\":{\"term\":{\"user\":\"kimchy\"}}}}]";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void testGetDataForJsonWithFilterAndRouting() throws JSONException, JsonProcessingException {
        AddAliasMapping addAliasMapping = new AddAliasMapping
                .Builder("tIndex", "tAlias")
                .setFilter(USER_FILTER_JSON)
                .addRouting("1")
                .build();
        String actualJson = objectMapper.writeValueAsString(addAliasMapping.getData());
        String expectedJson = "[{\"add\":{\"index\":\"tIndex\",\"alias\":\"tAlias\"," +
                "\"filter\":{\"term\":{\"user\":\"kimchy\"}},\"search_routing\":\"1\",\"index_routing\":\"1\"}}]";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

}
