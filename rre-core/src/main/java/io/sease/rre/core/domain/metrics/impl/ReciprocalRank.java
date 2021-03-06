package io.sease.rre.core.domain.metrics.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.sease.rre.Func;
import io.sease.rre.core.domain.metrics.Metric;
import io.sease.rre.core.domain.metrics.ValueFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * The reciprocal rank of a query response is the multiplicative inverse of the rank of the first correct answer.
 *
 * @author agazzarini
 * @since 1.0
 */
public class ReciprocalRank extends Metric {
    /**
     * Builds a new ReciprocalRank at X metric.
     */
    public ReciprocalRank() {
        super("RR@10");
    }

    @Override
    public ValueFactory createValueFactory(final String version) {
        return new ValueFactory(this, version) {
            private int rank;
            private int maxGain;

            @Override
            public void collect(final Map<String, Object> hit, final int rank, final String version) {
                judgment(id(hit))
                        .ifPresent(hitData -> {
                            final int gain = Func.gainOrRatingNode(hitData).map(JsonNode::asInt).orElse(2);
                            if (gain > maxGain) {
                                this.rank = rank;
                                this.maxGain = gain;
                            }
                        });
            }

            @Override
            public BigDecimal value() {
                if (relevantDocuments.size() == 0) {
                    return totalHits == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
                }
                if (rank == 0) {
                    return BigDecimal.ZERO;
                }

                return BigDecimal.ONE.divide(new BigDecimal(rank), 2, RoundingMode.HALF_UP);
            }
        };
    }
}