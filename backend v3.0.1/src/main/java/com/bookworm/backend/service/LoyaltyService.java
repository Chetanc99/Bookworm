package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.LoyaltyBalanceResponse;
import com.bookworm.backend.dto.response.LoyaltyLedgerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoyaltyService {

    Page<LoyaltyLedgerResponse> getHistory(Long userId, Pageable pageable);

    // Sums EARN minus REDEEM across the full ledger (not just the current page).
    LoyaltyBalanceResponse getBalance(Long userId);
}
