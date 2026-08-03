package com.bookworm.backend.service.impl;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pure rounding-safe percentage split - no repository/entity dependencies, so
 * it can be unit tested in isolation and reused later by a payout/statement
 * feature without dragging in persistence concerns.
 *
 * Independently rounding each beneficiary's share (grossAmount x pct / 100)
 * to 2dp can drift a cent or two away from the true total when percentages
 * don't divide evenly (e.g. three-way 33.33/33.33/33.34 splits of an odd
 * amount). This uses the largest-remainder method instead: floor every share
 * to 2dp, then hand out the leftover cents (targetTotal - sum(floors)) one at
 * a time to the shares with the largest fractional remainder, so
 * sum(results) always equals targetTotal exactly to the cent.
 *
 * targetTotal is grossAmount x (sum of the input percentages) / 100, rounded
 * HALF_UP to 2dp - i.e. the actual total royalty generated for this event
 * given whatever percentages are configured, not forced to grossAmount
 * itself (percentages need not sum to 100; see ProductBeneficiaryServiceImpl).
 */
@Component
public class RoyaltySplitCalculator {

    public record Share<T>(T key, BigDecimal percentage) {
    }

    public record Allocation<T>(T key, BigDecimal percentage, BigDecimal amount) {
    }

    public <T> List<Allocation<T>> split(BigDecimal grossAmount, List<Share<T>> shares) {
        if (shares.isEmpty()) {
            return List.of();
        }

        // High-precision raw share per beneficiary - not yet rounded to currency.
        List<BigDecimal> raw = shares.stream()
                .map(s -> grossAmount.multiply(s.percentage())
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                .toList();

        BigDecimal targetTotal = raw.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<BigDecimal> floors = raw.stream()
                .map(r -> r.setScale(2, RoundingMode.DOWN))
                .toList();
        BigDecimal floorSum = floors.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // Number of leftover cents to distribute; grossAmount/percentages are never
        // negative in this codebase (validated at the DTO/service boundary), so this
        // is always >= 0.
        int leftoverCents = targetTotal.subtract(floorSum)
                .movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();

        List<BigDecimal> remainders = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            remainders.add(raw.get(i).subtract(floors.get(i)));
        }

        // Indices sorted by largest remainder first; stable so equal-remainder ties
        // resolve in original (product-beneficiary insertion) order, keeping the
        // distribution deterministic rather than arbitrary.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) order.add(i);
        order.sort((a, b) -> remainders.get(b).compareTo(remainders.get(a)));

        BigDecimal[] amounts = floors.toArray(new BigDecimal[0]);
        BigDecimal cent = new BigDecimal("0.01");
        for (int i = 0; i < leftoverCents && i < order.size(); i++) {
            int idx = order.get(i);
            amounts[idx] = amounts[idx].add(cent);
        }

        List<Allocation<T>> result = new ArrayList<>(shares.size());
        for (int i = 0; i < shares.size(); i++) {
            result.add(new Allocation<>(shares.get(i).key(), shares.get(i).percentage(), amounts[i]));
        }
        return result;
    }
}
