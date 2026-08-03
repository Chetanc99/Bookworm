package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.LoyaltyLedger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LoyaltyLedgerResponse {
    private Long loyaltyLedgerId;
    private LoyaltyLedger.EntryType entryType;
    private Integer points;
    private String reason;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}
