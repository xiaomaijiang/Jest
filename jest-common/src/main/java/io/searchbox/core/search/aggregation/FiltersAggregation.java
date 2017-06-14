package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;

/**
 * @author cfstout
 */
public class FiltersAggregation extends BucketAggregation {

    private final static Logger log = LoggerFactory.getLogger(FiltersAggregation.class);
    public static final String TYPE = "filters";

    private String name;
    private Map<String, Bucket> bucketMap = new HashMap<String, Bucket>();
    private List<Bucket> bucketList = new LinkedList<Bucket>();

    public FiltersAggregation(String name, JsonNode filtersAggregation) {
        super(name, filtersAggregation);
        if (filtersAggregation.has(String.valueOf(BUCKETS))) {
            parseBuckets(filtersAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode buckets) {
        if (buckets.isArray()) {
            int elementNumber = 0;
            for (JsonNode bucket : buckets) {
                addBucket("filter" + Integer.toString(elementNumber++), bucket);
            }
        } else if (buckets.isObject()) {
            final Iterator<Map.Entry<String, JsonNode>> it = buckets.fields();
            while (it.hasNext()) {
                final Map.Entry<String, JsonNode> bucket = it.next();
                addBucket(bucket.getKey(), bucket.getValue());
            }
        } else {
            log.debug("Skipped bucket parsing because Buckets element of JSON was neither Object nor Array.");
        }
    }

    private void addBucket(String filterName, JsonNode bucketSource) {
        FilterAggregation bucket = new FilterAggregation(filterName, bucketSource);
        bucketMap.put(filterName, bucket);
        bucketList.add(bucket);
    }

    /**
     * Method for getting counts when filters when using anonymous filtering
     * @return A list of counts in the order that the filters were passed in
     */
    public List<Bucket> getBuckets() {
        return bucketList;
    }

    /**
     * Method for getting counts using named filters
     * @return A map filter names to associated counts
     */
    public Map<String, Bucket> getBucketMap() {
        return bucketMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        FiltersAggregation rhs = (FiltersAggregation) obj;
        return super.equals(obj) && Objects.equals(bucketMap, rhs.bucketMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bucketMap);
    }
}

