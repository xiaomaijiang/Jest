package io.searchbox.core.search.aggregation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

import static io.searchbox.core.search.aggregation.AggregationField.STD_DEVIATION;
import static io.searchbox.core.search.aggregation.AggregationField.SUM_OF_SQUARES;
import static io.searchbox.core.search.aggregation.AggregationField.VARIANCE;

/**
 * @author cfstout
 */
public class ExtendedStatsAggregation extends StatsAggregation {

    private Double sumOfSquares;
    private Double variance;
    private Double stdDeviation;

    public ExtendedStatsAggregation(String name, JsonNode extendedStatsAggregation) {
        super(name, extendedStatsAggregation);
        this.sumOfSquares = !extendedStatsAggregation.has(String.valueOf(SUM_OF_SQUARES)) || extendedStatsAggregation.get(String.valueOf(SUM_OF_SQUARES)).isNull() ?
                null : extendedStatsAggregation.get(String.valueOf(SUM_OF_SQUARES)).asDouble();
        this.variance = !extendedStatsAggregation.has(String.valueOf(VARIANCE)) || extendedStatsAggregation.get(String.valueOf(VARIANCE)).isNull() ?
                null : extendedStatsAggregation.get(String.valueOf(VARIANCE)).asDouble();
        this.stdDeviation = !extendedStatsAggregation.has(String.valueOf(STD_DEVIATION)) || extendedStatsAggregation.get(String.valueOf(STD_DEVIATION)).isNull() ?
                null : extendedStatsAggregation.get(String.valueOf(STD_DEVIATION)).asDouble();
    }

    /**
     * @return Sum of Squares for the aggregated data if found, null otherwise
     */
    public Double getSumOfSquares() {
        return sumOfSquares;
    }

    /**
     * @return Variance of the aggregated data if found, null otherwise
     */
    public Double getVariance() {
        return variance;
    }

    /**
     * @return Standard deviation of the aggregated data if found, null otherwise
     */
    public Double getStdDeviation() {
        return stdDeviation;
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

        ExtendedStatsAggregation rhs = (ExtendedStatsAggregation) obj;
        return super.equals(obj)
                && Objects.equals(stdDeviation, rhs.stdDeviation)
                && Objects.equals(sumOfSquares, rhs.sumOfSquares)
                && Objects.equals(variance, rhs.variance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                sumOfSquares,
                variance,
                stdDeviation);
    }
}
