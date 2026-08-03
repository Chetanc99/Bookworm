package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.RoyaltyLedger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RoyaltyLedgerResponse {
    private Long royaltyLedgerId;
    private Long beneficiaryId;
    private String beneficiaryName;
    private Long productId;
    private String productTitle;
    private RoyaltyLedger.SourceType sourceType;
    private Long sourceReferenceId;
    private BigDecimal grossAmount;
    private BigDecimal royaltyPercentage;
    private BigDecimal royaltyAmount;
    private RoyaltyLedger.PayoutStatus status;
    private LocalDateTime createdAt;
}
