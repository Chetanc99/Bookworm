package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RoyaltySummaryResponse {
    private Long beneficiaryId;
    private String beneficiaryName;
    private BigDecimal totalRoyaltyEarned;
    // Breakdown by RoyaltyLedger.status - both always populated (0 rather than
    // null when there's nothing in that bucket yet). No payout feature writes
    // PAID yet, so unpaidRoyalty == totalRoyaltyEarned today; the field exists
    // so the frontend doesn't need an API-shape change when payouts ship.
    private BigDecimal unpaidRoyalty;
    private BigDecimal paidRoyalty;
}
