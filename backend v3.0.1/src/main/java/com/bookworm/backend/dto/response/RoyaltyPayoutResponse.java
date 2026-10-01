package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Result of RoyaltyServiceImpl.markBeneficiaryRoyaltiesPaid - a summary of one payout run, not a persisted entity. */
@Getter
@Builder
@AllArgsConstructor
public class RoyaltyPayoutResponse {
    private Long beneficiaryId;
    private int royaltyLedgerRowsMarkedPaid;
    private BigDecimal totalAmountMarkedPaid;
    private LocalDateTime paidAt;
}
