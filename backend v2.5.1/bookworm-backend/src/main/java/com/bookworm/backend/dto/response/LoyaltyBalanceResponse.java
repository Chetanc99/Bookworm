package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Balance is derived by summing the ledger (EARN adds, REDEEM subtracts) -
 * never stored, matching LoyaltyLedger's "append-only, no cached total" design.
 */
@Getter
@Builder
@AllArgsConstructor
public class LoyaltyBalanceResponse {
    private Long userId;
    private Integer balance;
}
