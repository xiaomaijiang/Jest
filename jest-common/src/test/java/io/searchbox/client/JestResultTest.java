package io.searchbox.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.searchbox.annotations.JestId;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Dogukan Sonmez
 */
public class JestResultTest {
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final JestResult result = new JestResult(objectMapper);

    @Test
    public void extractGetResource() throws IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : {\n" +
                "        \"user\" : \"kimchy\",\n" +
                "        \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        Map<String, Object> expectedResultMap = new LinkedHashMap<String, Object>();
        expectedResultMap.put("user", "kimchy");
        expectedResultMap.put("postDate", "2009-11-15T14:12:12");
        expectedResultMap.put("message", "trying out Elastic Search");
        expectedResultMap.put(JestResult.ES_METADATA_ID, "1");
        JsonNode actualResultMap = result.extractSource().get(0);
        assertEquals(expectedResultMap.size(), actualResultMap.size());
        for (String key : expectedResultMap.keySet()) {
            assertEquals(expectedResultMap.get(key).toString(), actualResultMap.get(key).asText());
        }
    }

    @Test
    public void extractGetResourceWithoutMetadata() throws IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : {\n" +
                "        \"user\" : \"kimchy\",\n" +
                "        \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        Map<String, Object> expectedResultMap = new LinkedHashMap<String, Object>();
        expectedResultMap.put("user", "kimchy");
        expectedResultMap.put("postDate", "2009-11-15T14:12:12");
        expectedResultMap.put("message", "trying out Elastic Search");
        JsonNode actualResultMap = result.extractSource(false).get(0);
        assertEquals(expectedResultMap.size(), actualResultMap.size());
        for (String key : expectedResultMap.keySet()) {
            assertEquals(expectedResultMap.get(key).toString(), actualResultMap.get(key).asText());
        }
    }

    @Test
    public void extractGetResourceWithLongId() throws IOException {
        Long actualId = Integer.MAX_VALUE + 10L;

        String response = "{\n" +
                "    \"_index\" : \"blog\",\n" +
                "    \"_type\" : \"comment\",\n" +
                "    \"_id\" : \"" + actualId.toString() + "\", \n" +
                "    \"_source\" : {\n" +
                "        \"someIdName\" : \"" + actualId.toString() + "\"\n," +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);

        Comment actual = result.getSourceAsObject(Comment.class);
        assertNotNull(actual);

        assertEquals(new Long(Integer.MAX_VALUE + 10l), actual.getSomeIdName());
    }


    @Test
    public void extractGetResourceWithLongIdNotInSource() throws IOException {
        Long actualId = Integer.MAX_VALUE + 10l;

        String response = "{\n" +
                "    \"_index\" : \"blog\",\n" +
                "    \"_type\" : \"comment\",\n" +
                "    \"_id\" : \"" + actualId.toString() + "\", \n" +
                "    \"_source\" : {\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);

        SimpleComment actual = result.getSourceAsObject(SimpleComment.class);
        assertNotNull(actual);

        assertEquals(new Long(Integer.MAX_VALUE + 10l), actual.getSomeIdName());
    }



    @Test
    public void extractUnFoundGetResource() throws IOException {
        String response = "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"13333\",\"exists\":false}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        List<JsonNode> resultList = result.extractSource();
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void getGetSourceAsObject() throws IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : {\n" +
                "        \"user\" : \"kimchy\",\n" +
                "        \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);
        Twitter twitter = result.getSourceAsObject(Twitter.class);
        assertNotNull(twitter);
        assertEquals("kimchy", twitter.getUser());
        assertEquals("trying out Elastic Search", twitter.getMessage());
        assertEquals("2009-11-15T14:12:12", twitter.getPostDate());
    }

    @Test
    public void getGetSourceAsObjectWithoutMetadata() throws IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : {\n" +
                "        \"user\" : \"kimchy\",\n" +
                "        \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);
        Map twitter = result.getSourceAsObject(Map.class, false);
        assertNotNull(twitter);
        assertEquals("kimchy", twitter.get("user"));
        assertEquals("trying out Elastic Search", twitter.get("message"));
        assertEquals("2009-11-15T14:12:12", twitter.get("postDate"));
        assertNull(twitter.get(JestResult.ES_METADATA_ID));
        assertNull(twitter.get(JestResult.ES_METADATA_VERSION));
    }

    @Test
    public void getGetSourceAsString() throws JSONException, IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : {\n" +
                "        \"user\" : \"kimchy\",\n" +
                "        \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "        \"message\" : \"trying out Elastic Search\"\n" +
                "    }\n" +
                "}\n";

        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);

        String onlySource = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2009-11-15T14:12:12\"," +
                "\"message\":\"trying out Elastic Search\"" +
                "}";
        JSONAssert.assertEquals(onlySource, result.getSourceAsString(), false);
    }

    @Test
    public void getGetSourceAsStringArray() throws JSONException, IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\", \n" +
                "    \"_source\" : [" +
                "        { \"user\" : \"kimch\" }, " +
                "        { \"user\" : \"bello\" }," +
                "        { \"user\" : \"ionex\" }" +
                "    ]\n" +
                "}\n";

        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);

        String onlySource = "[" +
                "{\"user\":\"kimch\"}," +
                "{\"user\":\"bello\"}," +
                "{\"user\":\"ionex\"}" +
                "]";
        JSONAssert.assertEquals(onlySource, result.getSourceAsString(), false);
    }

    @Test
    public void getGetSourceAsStringNoResult() throws IOException {
        String response = "{\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\" \n" +
                "}\n";

        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        result.setSucceeded(true);
        assertNull(result.getSourceAsString());
    }

    @Test
    public void getUnFoundGetResultAsAnObject() throws IOException {
        String response = "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"13333\",\"exists\":false}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("_source");
        assertNull(result.getSourceAsObject(Twitter.class));
    }


    @Test
    public void extractUnFoundMultiGetResource() throws IOException {
        String response = "{\n" +
                "\n" +
                "\"docs\":\n" +
                "[\n" +
                "{\"_index\":\"test\",\"_type\":\"type\",\"_id\":\"1\",\"exists\":false},\n" +
                "{\"_index\":\"test\",\"_type\":\"type\",\"_id\":\"2\",\"exists\":false}\n" +
                "]\n" +
                "\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("docs/_source");

        List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
        List<JsonNode> actual = result.extractSource();
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void extractMultiGetWithSourcePartlyFound() throws IOException {
        String response = "{\"docs\":" +
                "[" +
                "{\"_index\":\"test\",\"_type\":\"type\",\"_id\":\"2\",\"exists\":false},\n" +
                "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"2\",\"_version\":2,\"exists\":true, " +
                "\"_source\" : {\n" +
                "    \"user\" : \"kimchy\",\n" +
                "    \"post_date\" : \"2009-11-15T14:12:12\",\n" +
                "    \"message\" : \"trying out Elastic Search\"\n" +
                "}}" +
                "]}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("docs/_source");
        List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
        Map<String, Object> expectedMap1 = new LinkedHashMap<String, Object>();
        expectedMap1.put("user", "kimchy");
        expectedMap1.put("post_date", "2009-11-15T14:12:12");
        expectedMap1.put("message", "trying out Elastic Search");
        expected.add(expectedMap1);
        List<JsonNode> actual = result.extractSource();
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Map<String, Object> expectedMap = expected.get(i);
            JsonNode actualMap = actual.get(i);
            for (String key : expectedMap.keySet()) {
                assertEquals(expectedMap.get(key).toString(), actualMap.get(key).asText());
            }
        }
    }

    @Test
    public void extractMultiGetWithSource() throws IOException {
        String response = "{\"docs\":" +
                "[" +
                "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"1\",\"_version\":9,\"exists\":true, " +
                "\"_source\" : {\n" +
                "    \"user\" : \"kimchy\",\n" +
                "    \"post_date\" : \"2009-11-15T14:12:12\",\n" +
                "    \"message\" : \"trying out Elastic Search\"\n" +
                "}}," +
                "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"2\",\"_version\":2,\"exists\":true, " +
                "\"_source\" : {\n" +
                "    \"user\" : \"kimchy\",\n" +
                "    \"post_date\" : \"2009-11-15T14:12:12\",\n" +
                "    \"message\" : \"trying out Elastic Search\"\n" +
                "}}" +
                "]}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("docs/_source");

        List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
        Map<String, Object> expectedMap1 = new LinkedHashMap<String, Object>();
        expectedMap1.put("user", "kimchy");
        expectedMap1.put("post_date", "2009-11-15T14:12:12");
        expectedMap1.put("message", "trying out Elastic Search");

        Map<String, Object> expectedMap2 = new LinkedHashMap<String, Object>();
        expectedMap2.put("user", "kimchy");
        expectedMap2.put("post_date", "2009-11-15T14:12:12");
        expectedMap2.put("message", "trying out Elastic Search");

        expected.add(expectedMap1);
        expected.add(expectedMap2);

        List<JsonNode> actual = result.extractSource();

        for (int i = 0; i < expected.size(); i++) {
            Map<String, Object> expectedMap = expected.get(i);
            JsonNode actualMap = actual.get(i);
            for (String key : expectedMap.keySet()) {
                assertEquals(expectedMap.get(key).toString(), actualMap.get(key).asText());
            }
        }
    }

    @Test
    public void getMultiGetSourceAsObject() throws IOException {
        String response = "{\"docs\":" +
                "[" +
                "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"1\",\"_version\":9,\"exists\":true, " +
                "\"_source\" : {\n" +
                "    \"user\" : \"kimchy\",\n" +
                "    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "    \"message\" : \"trying out Elastic Search\"\n" +
                "}}," +
                "{\"_index\":\"twitter\",\"_type\":\"tweet\",\"_id\":\"2\",\"_version\":2,\"exists\":true, " +
                "\"_source\" : {\n" +
                "    \"user\" : \"dogukan\",\n" +
                "    \"postDate\" : \"2012\",\n" +
                "    \"message\" : \"My message\"\n" +
                "}}" +
                "]}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("docs/_source");
        result.setSucceeded(true);

        List<Twitter> twitterList = result.getSourceAsObjectList(Twitter.class);

        assertEquals(2, twitterList.size());

        assertEquals("kimchy", twitterList.get(0).getUser());
        assertEquals("trying out Elastic Search", twitterList.get(0).getMessage());
        assertEquals("2009-11-15T14:12:12", twitterList.get(0).getPostDate());

        assertEquals("dogukan", twitterList.get(1).getUser());
        assertEquals("My message", twitterList.get(1).getMessage());
        assertEquals("2012", twitterList.get(1).getPostDate());
    }

    @Test
    public void getUnFoundMultiGetSourceAsObject() throws IOException {
        String response = "{\n" +
                "\n" +
                "\"docs\":\n" +
                "[\n" +
                "{\"_index\":\"test\",\"_type\":\"type\",\"_id\":\"1\",\"exists\":false},\n" +
                "{\"_index\":\"test\",\"_type\":\"type\",\"_id\":\"2\",\"exists\":false}\n" +
                "]\n" +
                "\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("docs/_source");
        result.setSucceeded(true);
        List<Twitter> twitterList = result.getSourceAsObjectList(Twitter.class);
        assertEquals(0, twitterList.size());
    }


    @Test
    public void extractEmptySearchSource() throws IOException {
        String response = "{\"took\":60,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1," +
                "\"failed\":0},\"hits\":{\"total\":0,\"max_score\":null,\"hits\":[]}}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("hits/hits/_source");
        List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
        List<JsonNode> actual = result.extractSource();
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void extractSearchSource() throws IOException {
        String response = "{\n" +
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
                "                \"_id\" : \"1\", \n" +
                "                \"_version\" : \"2\", \n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elastic Search\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("hits/hits/_source");
        Map<String, Object> expectedResultMap = new LinkedHashMap<String, Object>();
        expectedResultMap.put("user", "kimchy");
        expectedResultMap.put("postDate", "2009-11-15T14:12:12");
        expectedResultMap.put("message", "trying out Elastic Search");
        JsonNode actualResultMap = result.extractSource().get(0);
        assertEquals(expectedResultMap.size() + 2, actualResultMap.size());
        for (String key : expectedResultMap.keySet()) {
            assertEquals(expectedResultMap.get(key).toString(), actualResultMap.get(key).asText());
        }
    }

    @Test
    public void getSearchSourceAsObject() throws IOException {
        String response = "{\n" +
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
                "                \"_id\" : \"1\", \n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elastic Search\"\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"_index\" : \"twitter\",\n" +
                "                \"_type\" : \"tweet\",\n" +
                "                \"_id\" : \"1\", \n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"dogukan\",\n" +
                "                    \"postDate\" : \"2012\",\n" +
                "                    \"message\" : \"My Search Result\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("hits/hits/_source");
        result.setSucceeded(true);
        List<Twitter> twitterList = result.getSourceAsObjectList(Twitter.class);
        assertEquals(2, twitterList.size());
        assertEquals("kimchy", twitterList.get(0).getUser());
        assertEquals("trying out Elastic Search", twitterList.get(0).getMessage());
        assertEquals("2009-11-15T14:12:12", twitterList.get(0).getPostDate());
        assertEquals("dogukan", twitterList.get(1).getUser());
        assertEquals("My Search Result", twitterList.get(1).getMessage());
        assertEquals("2012", twitterList.get(1).getPostDate());
    }

    @Test
    public void getSearchSourceAsObjectWithoutMetadata() throws IOException {
        String response = "{\n" +
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
                "                \"_id\" : \"1\", \n" +
                "                \"_source\" : {\n" +
                "                    \"user\" : \"kimchy\",\n" +
                "                    \"postDate\" : \"2009-11-15T14:12:12\",\n" +
                "                    \"message\" : \"trying out Elastic Search\"\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("hits/hits/_source");
        result.setSucceeded(true);
        List<Map> twitterList = result.getSourceAsObjectList(Map.class, false);
        assertEquals(1, twitterList.size());
        assertEquals("kimchy", twitterList.get(0).get("user"));
        assertEquals("trying out Elastic Search", twitterList.get(0).get("message"));
        assertEquals("2009-11-15T14:12:12", twitterList.get(0).get("postDate"));
        assertNull(twitterList.get(0).get(JestResult.ES_METADATA_ID));
        assertNull(twitterList.get(0).get(JestResult.ES_METADATA_VERSION));
    }


    @Test
    public void extractIndexSource() throws IOException {
        String response = "{\n" +
                "    \"ok\" : true,\n" +
                "    \"_index\" : \"twitter\",\n" +
                "    \"_type\" : \"tweet\",\n" +
                "    \"_id\" : \"1\"\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
        Map<String, Object> expectedMap = new LinkedHashMap<String, Object>();
        expectedMap.put("ok", true);
        expectedMap.put("_index", "twitter");
        expectedMap.put("_type", "tweet");
        expectedMap.put("_id", "1");
        expected.add(expectedMap);
        List<JsonNode> actual = result.extractSource();
        for (int i = 0; i < expected.size(); i++) {
            Map<String, Object> map = expected.get(i);
            JsonNode actualMap = actual.get(i);
            for (String key : map.keySet()) {
                assertEquals(map.get(key).toString(), actualMap.get(key).asText());
            }
        }
    }

    @Test
    public void extractCountResult() throws IOException {
        String response = "{\n" +
                "    \"count\" : 1,\n" +
                "    \"_shards\" : {\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("count");
        Double actual = result.extractSource().get(0).asDouble();
        assertEquals(1.0, actual, 0.01);
    }

    @Test
    public void getCountSourceAsObject() throws IOException {
        String response = "{\n" +
                "    \"count\" : 1,\n" +
                "    \"_shards\" : {\n" +
                "        \"total\" : 5,\n" +
                "        \"successful\" : 5,\n" +
                "        \"failed\" : 0\n" +
                "    }\n" +
                "}\n";
        result.setJsonMap(objectMapper.readValue(response, TYPE_REFERENCE));
        result.setPathToResult("count");
        result.setSucceeded(true);
        Double count = result.getSourceAsObject(Double.class);
        assertEquals(1.0, count, 0.01);
    }

    @Test
    public void getKeysWithPathToResult() {
        result.setPathToResult("_source");
        String[] expected = {"_source"};
        String[] actual = result.getKeys();
        assertEquals(1, actual.length);
        assertEquals(expected[0], actual[0]);
    }

    @Test
    public void getKeysWithoutPathToResult() {
        assertNull(result.getKeys());
    }

    private static class Twitter {
        String user;

        String postDate;

        String message;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPostDate() {
            return postDate;
        }

        public void setPostDate(String postDate) {
            this.postDate = postDate;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    static abstract class Base {

        @JestId
        Long someIdName;

        public Long getSomeIdName() {
            return someIdName;
        }

        public void setSomeIdName(Long someIdName) {
            this.someIdName = someIdName;
        }
    }

    private static class Comment extends Base {
        String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private static class SimpleComment {

        @JestId
        Long someIdName;

        String message;

        public Long getSomeIdName() {
            return someIdName;
        }

        public void setSomeIdName(Long someIdName) {
            this.someIdName = someIdName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
