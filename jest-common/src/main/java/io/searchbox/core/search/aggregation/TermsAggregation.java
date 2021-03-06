package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT_ERROR_UPPER_BOUND;
import static io.searchbox.core.search.aggregation.AggregationField.KEY;
import static io.searchbox.core.search.aggregation.AggregationField.KEY_AS_STRING;
import static io.searchbox.core.search.aggregation.AggregationField.SUM_OTHER_DOC_COUNT;

;

/**
 * @author cfstout
 */

public class TermsAggregation extends BucketAggregation {

    public static final String TYPE = "terms";

    private Long docCountErrorUpperBound;
    private Long sumOtherDocCount;
    private List<Entry> buckets = new LinkedList<Entry>();

    public TermsAggregation(String name, JsonNode termAggregation) {
        super(name, termAggregation);
        if (termAggregation.has(String.valueOf(DOC_COUNT_ERROR_UPPER_BOUND))) {
            docCountErrorUpperBound = termAggregation.get(String.valueOf(DOC_COUNT_ERROR_UPPER_BOUND)).asLong();
        }
        if (termAggregation.has(String.valueOf(SUM_OTHER_DOC_COUNT))) {
            sumOtherDocCount = termAggregation.get(String.valueOf(SUM_OTHER_DOC_COUNT)).asLong();
        }

        if (termAggregation.has(String.valueOf(BUCKETS)) && termAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(termAggregation.get(String.valueOf(BUCKETS)));
        }
    }

    private void parseBuckets(JsonNode bucketsSource) {
        for(JsonNode bucketElement : bucketsSource) {
            ObjectNode bucket = (ObjectNode) bucketElement;
            if (bucket.has(String.valueOf(KEY_AS_STRING))) {
            	buckets.add(new Entry(bucket, bucket.get(String.valueOf(KEY)).asText(), bucket.get(String.valueOf(KEY_AS_STRING)).asText(), bucket.get(String.valueOf(DOC_COUNT)).asLong()));
            } else {
            	buckets.add(new Entry(bucket, bucket.get(String.valueOf(KEY)).asText(), bucket.get(String.valueOf(DOC_COUNT)).asLong()));
            }
        }
    }

    public Long getDocCountErrorUpperBound() {
        return docCountErrorUpperBound;
    }

    public Long getSumOtherDocCount() {
        return sumOtherDocCount;
    }

    public List<Entry> getBuckets() {
        return buckets;
    }

    public class Entry extends Bucket {
        private String key;
        private String keyAsString;

        public Entry(ObjectNode bucket, String key, Long count) {
            this(bucket, key, key, count);
        }

        public Entry(ObjectNode bucket, String key, String keyAsString, Long count) {
        	super(bucket, count);
        	this.key = key;
        	this.keyAsString = keyAsString;
        }

        public String getKey() {
            return key;
        }

        public String getKeyAsString() {
        	return keyAsString;
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

            Entry rhs = (Entry) obj;
            return super.equals(obj)
                    && Objects.equals(key, rhs.key)
                    && Objects.equals(keyAsString, rhs.keyAsString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCount(), getKey(), keyAsString);
        }
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

        TermsAggregation rhs = (TermsAggregation) obj;
        return super.equals(obj)
                && Objects.equals(buckets, rhs.buckets)
                && Objects.equals(docCountErrorUpperBound, rhs.docCountErrorUpperBound)
                && Objects.equals(sumOtherDocCount, rhs.sumOtherDocCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                docCountErrorUpperBound,
                sumOtherDocCount,
                buckets);
    }
}
