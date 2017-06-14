package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.BG_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.BUCKETS;
import static io.searchbox.core.search.aggregation.AggregationField.DOC_COUNT;
import static io.searchbox.core.search.aggregation.AggregationField.KEY;
import static io.searchbox.core.search.aggregation.AggregationField.SCORE;

/**
 * @author cfstout
 */
public class SignificantTermsAggregation extends BucketAggregation {

    public static final String TYPE = "significant_terms";

    private Long totalCount;
    private List<SignificantTerm> significantTerms = new LinkedList<SignificantTerm>();

    public SignificantTermsAggregation(String name, JsonNode significantTermsAggregation) {
        super(name, significantTermsAggregation);

        if (significantTermsAggregation.has(String.valueOf(BUCKETS)) && significantTermsAggregation.get(String.valueOf(BUCKETS)).isArray()) {
            parseBuckets(significantTermsAggregation.get(String.valueOf(BUCKETS)));
        }
        if (significantTermsAggregation.has(String.valueOf(DOC_COUNT))) {
            totalCount = significantTermsAggregation.get(String.valueOf(DOC_COUNT)).asLong();
        }
    }

    private void parseBuckets(JsonNode bucketsSource) {
        for (JsonNode bucket : bucketsSource) {
            SignificantTerm term = new SignificantTerm(
                    bucket,
                    bucket.get(String.valueOf(KEY)).asText(),
                    bucket.get(String.valueOf(DOC_COUNT)).asLong(),
                    bucket.get(String.valueOf(SCORE)).asDouble(),
                    bucket.get(String.valueOf(BG_COUNT)).asLong()
            );
            significantTerms.add(term);
        }
    }

    /**
     * @return total count of documents matching foreground aggregation if found, null otherwise
     */
    public Long getTotalCount() {
        return totalCount;
    }

    public List<SignificantTerm> getBuckets() {
        return significantTerms;
    }

    public class SignificantTerm extends Bucket {
        private String key;
        private Double score;
        private Long backgroundCount;

        public SignificantTerm(JsonNode bucket, String key, Long count, Double score, Long backgroundCount) {
            super(bucket, count);
            this.key = key;
            this.score = score;
            this.backgroundCount = backgroundCount;
        }

        public String getKey() {
            return key;
        }

        public Double getScore() {
            return score;
        }

        public Long getBackgroundCount() {
            return backgroundCount;
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

            SignificantTerm rhs = (SignificantTerm) obj;
            return super.equals(obj)
                    && Objects.equals(key, rhs.key)
                    && Objects.equals(score, rhs.score)
                    && Objects.equals(backgroundCount, rhs.backgroundCount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), backgroundCount, key, score);
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

        SignificantTermsAggregation rhs = (SignificantTermsAggregation) obj;
        return super.equals(obj)
                && Objects.equals(totalCount, rhs.totalCount)
                && Objects.equals(significantTerms, rhs.significantTerms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), totalCount, significantTerms);
    }
}
