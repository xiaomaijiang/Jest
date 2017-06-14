package io.searchbox.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.searchbox.annotations.JestId;
import io.searchbox.annotations.JestVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Dogukan Sonmez
 */
public class JestResult {
    public static final String ES_METADATA_ID = "es_metadata_id";
    public static final String ES_METADATA_VERSION = "es_metadata_version";

    private static final Logger log = LoggerFactory.getLogger(JestResult.class);
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };

    protected static class MetaField {
        public final String internalFieldName;
        public final String esFieldName;
        public final Class<? extends Annotation> annotationClass;

        MetaField(String internalFieldName, String esFieldName, Class<? extends Annotation> annotationClass) {
            this.internalFieldName = internalFieldName;
            this.esFieldName = esFieldName;
            this.annotationClass = annotationClass;
        }
    }

    protected static final ImmutableList<MetaField> META_FIELDS = ImmutableList.of(
            new MetaField(ES_METADATA_ID, "_id", JestId.class),
            new MetaField(ES_METADATA_VERSION, "_version", JestVersion.class)
    );

    protected JsonNode jsonObject;
    protected String jsonString;
    protected String pathToResult;
    protected int responseCode;
    protected boolean isSucceeded;
    protected String errorMessage;
    protected ObjectMapper objectMapper;

    private JestResult() {
    }

    public JestResult(JestResult source) {
        this.jsonObject = source.jsonObject;
        this.jsonString = source.jsonString;
        this.pathToResult = source.pathToResult;
        this.responseCode = source.responseCode;
        this.isSucceeded = source.isSucceeded;
        this.errorMessage = source.errorMessage;
        this.objectMapper = source.objectMapper;
    }

    public JestResult(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getPathToResult() {
        return pathToResult;
    }

    public void setPathToResult(String pathToResult) {
        this.pathToResult = pathToResult;
    }

    public Object getValue(String key) {
        return getJsonMap().get(key);
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public void setSucceeded(boolean succeeded) {
        isSucceeded = succeeded;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * manually set an error message, eg. for the cases where non-200 response code is received
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JsonNode getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JsonNode jsonObject) {
        this.jsonObject = jsonObject;
        if (jsonObject.get("error") != null) {
            errorMessage = jsonObject.get("error").toString();
        }
    }

    @Deprecated
    public Map<String, Map> getJsonMap() {
        return objectMapper.convertValue(jsonObject, TYPE_REFERENCE);
    }

    public void setJsonMap(Map<String, Object> resultMap) {
        final JsonNode jsonNode = objectMapper.convertValue(resultMap, JsonNode.class);
        setJsonObject(jsonNode);
    }

    /**
     * @return null if operation did not succeed or the response is null or the "keys" field of the action is empty or
     * the response does not contain the key to source.
     * String representing the source JSON element(s) otherwise.
     * Elements are joined with a comma if there are multiple sources (e.g.: search with multiple hits).
     */
    public String getSourceAsString() {
        List<String> sources = getSourceAsStringList();
        return sources == null ? null : Joiner.on(',').join(sources);
    }

    /**
     * @return null if operation did not succeed or the response is null or the "keys" field of the action is empty or
     * the response does not contain the key to source.
     * List of strings representing the source JSON element(s) otherwise.
     */
    public List<String> getSourceAsStringList() {
        String[] keys = getKeys();
        if (!isSucceeded || jsonObject == null || keys == null || keys.length == 0 || !jsonObject.has(keys[0])) {
            return null;
        }

        List<String> sourceList = new ArrayList<String>();
        for (JsonNode element : extractSource(false)) {
            sourceList.add(element.toString());
        }
        return sourceList;
    }

    public <T> T getSourceAsObject(Class<T> clazz) {
        return getSourceAsObject(clazz, true);
    }

    public <T> T getSourceAsObject(Class<T> clazz, boolean addEsMetadataFields) {
        T sourceAsObject = null;

        List<T> sources = getSourceAsObjectList(clazz, addEsMetadataFields);
        if (sources.size() > 0) {
            sourceAsObject = sources.get(0);
        }

        return sourceAsObject;
    }

    public <T> List<T> getSourceAsObjectList(Class<T> type) {
        return getSourceAsObjectList(type, true);
    }

    public <T> List<T> getSourceAsObjectList(Class<T> type, boolean addEsMetadataFields) {
        List<T> objectList = new ArrayList<T>();

        if (isSucceeded) {
            for (JsonNode source : extractSource(addEsMetadataFields)) {
                T obj = createSourceObject(source, type);
                if (obj != null) {
                    objectList.add(obj);
                }
            }
        }

        return objectList;
    }

    protected List<JsonNode> extractSource() {
        return extractSource(true);
    }

    protected List<JsonNode> extractSource(boolean addEsMetadataFields) {
        List<JsonNode> sourceList = new ArrayList<JsonNode>();

        if (jsonObject != null) {
            String[] keys = getKeys();
            if (keys == null) {
                sourceList.add(jsonObject);
            } else {
                String sourceKey = keys[keys.length - 1];
                JsonNode obj = jsonObject.get(keys[0]);
                if (keys.length > 1) {
                    for (int i = 1; i < keys.length - 1; i++) {
                        obj = ((ObjectNode) obj).get(keys[i]);
                    }

                    if (obj.isObject()) {
                        JsonNode source = obj.get(sourceKey);
                        if (source != null) {
                            sourceList.add(source);
                        }
                    } else if (obj.isArray()) {
                        for (JsonNode element : obj) {
                            if (element instanceof ObjectNode) {
                                JsonNode currentObj = element;
                                JsonNode source = currentObj.get(sourceKey);
                                if (source != null) {
                                    ObjectNode copy = source.deepCopy();
                                    if (addEsMetadataFields) {
                                        for (MetaField metaField : META_FIELDS) {
                                            copy.set(metaField.internalFieldName, currentObj.get(metaField.esFieldName));
                                        }
                                    }
                                    sourceList.add(copy);
                                }
                            }
                        }
                    }
                } else if (obj != null) {
                    JsonNode copy = obj.deepCopy();
                    if (addEsMetadataFields && copy.isObject()) {
                        for (MetaField metaField : META_FIELDS) {
                            JsonNode metaElement = jsonObject.get(metaField.esFieldName);
                            if (metaElement != null) {
                                ((ObjectNode) copy).set(metaField.internalFieldName, metaElement);
                            }
                        }
                    }
                    sourceList.add(copy);
                }
            }
        }

        return sourceList;
    }

    protected <T> T createSourceObject(JsonNode source, Class<T> type) {
        T obj = null;
        try {
            obj = objectMapper.convertValue(source, type);

            // Check if JestId is visible
            Class clazz = type;
            int knownMetadataFieldsCount = META_FIELDS.size();
            int foundFieldsCount = 0;
            boolean allFieldsFound = false;
            while (clazz != null && !allFieldsFound) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (foundFieldsCount == knownMetadataFieldsCount) {
                        allFieldsFound = true;
                        break;
                    }
                    for (MetaField metaField : META_FIELDS) {
                        if (field.isAnnotationPresent(metaField.annotationClass) && setAnnotatedField(obj, source, field, metaField.internalFieldName)) {
                            foundFieldsCount++;
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }

        } catch (Exception e) {
            log.error("Unhandled exception occurred while converting source to the object. " + type.getCanonicalName(), e);
        }
        return obj;
    }

    private <T> boolean setAnnotatedField(T obj, JsonNode source, Field field, String fieldName) {
        try {
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value == null) {
                Class<?> fieldType = field.getType();
                JsonNode element = ((ObjectNode) source).get(fieldName);
                field.set(obj, getAs(element, fieldType));
                return true;
            }
        } catch (IllegalAccessException e) {
            log.error("Unhandled exception occurred while setting annotated field from source");
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> T getAs(JsonNode id, Class<T> fieldType) throws IllegalAccessException {
        if (id.isNull()) {
            return null;
        }
        if (fieldType.isAssignableFrom(String.class)) {
            return (T) id.asText();
        }
        if (fieldType.isAssignableFrom(Number.class)) {
            return (T) id.numberValue();
        }
        if (fieldType.isAssignableFrom(BigDecimal.class)) {
            return (T) id.decimalValue();
        }
        if (fieldType.isAssignableFrom(Double.class)) {
            Object o = id.asDouble();
            return (T) o;
        }
        if (fieldType.isAssignableFrom(Float.class)) {
            Object o = id.floatValue();
            return (T) o;
        }
        if (fieldType.isAssignableFrom(BigInteger.class)) {
            return (T) id.bigIntegerValue();
        }
        if (fieldType.isAssignableFrom(Long.class)) {
            Object o = id.asLong();
            return (T) o;
        }
        if (fieldType.isAssignableFrom(Integer.class)) {
            Object o = id.asInt();
            return (T) o;
        }
        if (fieldType.isAssignableFrom(Short.class)) {
            Object o = id.shortValue();
            return (T) o;
        }
        if (fieldType.isAssignableFrom(Character.class)) {
            return (T) (Character) (char) id.intValue();
        }
        if (fieldType.isAssignableFrom(Byte.class)) {
            return (T) (Byte) id.numberValue().byteValue();
        }
        if (fieldType.isAssignableFrom(Boolean.class)) {
            return (T) (Boolean) id.asBoolean();
        }

        throw new RuntimeException("cannot assign " + id + " to " + fieldType);
    }

    protected String[] getKeys() {
        return pathToResult == null ? null : pathToResult.split("/");
    }

}
