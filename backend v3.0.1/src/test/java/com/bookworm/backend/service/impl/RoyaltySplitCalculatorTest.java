package com.bookworm.backend.service.impl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoyaltySplitCalculatorTest {

    private final RoyaltySplitCalculator calculator = new RoyaltySplitCalculator();

    @Test
    void split_evenPercentages_noRoundingNeeded() {
        var shares = List.of(
                new RoyaltySplitCalculator.Share<>("A", new BigDecimal("70.00")),
                new RoyaltySplitCalculator.Share<>("B", new BigDecimal("30.00")));

        var result = calculator.split(new BigDecimal("100.00"), shares);

        assertThat(result.get(0).amount()).isEqualByComparingTo("70.00");
        assertThat(result.get(1).amount()).isEqualByComparingTo("30.00");
        assertThat(sumOf(result)).isEqualByComparingTo("100.00");
    }

    @Test
    void split_thirds_distributesLeftoverCentToLargestRemainder() {
        var shares = List.of(
                new RoyaltySplitCalculator.Share<>("A", new BigDecimal("33.34")),
                new RoyaltySplitCalculator.Share<>("B", new BigDecimal("33.33")),
                new RoyaltySplitCalculator.Share<>("C", new BigDecimal("33.33")));

        var result = calculator.split(new BigDecimal("10.00"), shares);

        // Naive independent HALF_UP rounding would give 3.33/3.33/3.33 = 9.99.
        assertThat(sumOf(result)).isEqualByComparingTo("10.00");
        assertThat(result.get(0).amount()).isEqualByComparingTo("3.34"); // largest remainder
        assertThat(result.get(1).amount()).isEqualByComparingTo("3.33");
        assertThat(result.get(2).amount()).isEqualByComparingTo("3.33");
    }

    @Test
    void split_zeroGrossAmount_returnsZeroForEveryShareButStillOneEntryEach() {
        var shares = List.of(
                new RoyaltySplitCalculator.Share<>("A", new BigDecimal("50.00")),
                new RoyaltySplitCalculator.Share<>("B", new BigDecimal("50.00")));

        var result = calculator.split(BigDecimal.ZERO, shares);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).amount()).isEqualByComparingTo("0.00");
        assertThat(result.get(1).amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void split_percentagesNotSummingTo100_onlyDistributesTheAllocatedPortion() {
        // 60% allocated total on a 100.00 gross -> 60.00 total royalty, not 100.00.
        var shares = List.of(
                new RoyaltySplitCalculator.Share<>("A", new BigDecimal("40.00")),
                new RoyaltySplitCalculator.Share<>("B", new BigDecimal("20.00")));

        var result = calculator.split(new BigDecimal("100.00"), shares);

        assertThat(sumOf(result)).isEqualByComparingTo("60.00");
    }

    @Test
    void split_emptyShares_returnsEmptyList() {
        assertThat(calculator.split(new BigDecimal("100.00"), List.of())).isEmpty();
    }

    private BigDecimal sumOf(List<RoyaltySplitCalculator.Allocation<String>> result) {
        return result.stream().map(RoyaltySplitCalculator.Allocation::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
