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
public class RemoveAliasMappingTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final Map<String, Object> USER_FILTER_JSON = new MapBuilder<String, Object>()
            .put("term", MapBuilder.newMapBuilder()
                    .put("user", "kimchy")
                    .immutableMap())
            .immutableMap();

    @Test
    public void testBasicGetDataForJson() throws JSONException, JsonProcessingException {
        RemoveAliasMapping addAliasMapping = new RemoveAliasMapping
                .Builder("tIndex", "tAlias")
                .build();
        String actualJson = objectMapper.writeValueAsString(addAliasMapping.getData());
        String expectedJson = "[{\"remove\":{\"index\":\"tIndex\",\"alias\":\"tAlias\"}}]";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void testGetDataForJsonWithFilter() throws JSONException, JsonProcessingException {
        RemoveAliasMapping addAliasMapping = new RemoveAliasMapping
                .Builder("tIndex", "tAlias")
                .setFilter(USER_FILTER_JSON)
                .build();
        String actualJson = objectMapper.writeValueAsString(addAliasMapping.getData());
        String expectedJson = "[{\"remove\":{\"index\":\"tIndex\",\"alias\":\"tAlias\",\"filter\":{\"term\":{\"user\":\"kimchy\"}}}}]";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void testGetDataForJsonWithFilterAndRouting() throws JSONException, JsonProcessingException {
        RemoveAliasMapping addAliasMapping = new RemoveAliasMapping
                .Builder("tIndex", "tAlias")
                .setFilter(USER_FILTER_JSON)
                .addRouting("1")
                .build();
        String actualJson = objectMapper.writeValueAsString(addAliasMapping.getData());
        String expectedJson = "[{\"remove\":{\"index\":\"tIndex\",\"alias\":\"tAlias\",\"filter\":{\"term\":{\"user\":\"kimchy\"}},\"search_routing\":\"1\",\"index_routing\":\"1\"}}]";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

}
