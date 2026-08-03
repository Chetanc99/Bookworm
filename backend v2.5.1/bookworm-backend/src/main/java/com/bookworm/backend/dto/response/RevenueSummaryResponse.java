package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Admin dashboard revenue aggregate. See AdminTransactionRepository.getRevenueSummary
 * for exactly which transaction statuses count toward totalRevenue.
 */
@Getter
@Builder
@AllArgsConstructor
public class RevenueSummaryResponse {
    private BigDecimal totalRevenue;
    private long purchaseCount;
    private long rentCount;
}
