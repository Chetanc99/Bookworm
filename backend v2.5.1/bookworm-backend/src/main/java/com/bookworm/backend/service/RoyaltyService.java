package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.RoyaltyLedgerResponse;
import com.bookworm.backend.dto.response.RoyaltySummaryResponse;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.RoyaltyLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoyaltyService {

    /**
     * Splits grossAmount across every Beneficiary configured for the
     * product (via ProductBeneficiary.royaltyPercentage) and writes one
     * RoyaltyLedger row per beneficiary. Called by Purchase/Rent checkout
     * and by UserLibrary borrow, right after their own transaction/loan
     * row is persisted - never called standalone from a controller, since
     * it always needs a real revenue event and gross amount to split.
     */
    void recordForProduct(Product product, java.math.BigDecimal grossAmount,
                           RoyaltyLedger.SourceType sourceType, Long sourceReferenceId);

    Page<RoyaltyLedgerResponse> getHistory(Long beneficiaryId, Pageable pageable);

    /**
     * Every beneficiary's row for one revenue/usage event (one PurchaseItem,
     * RentTransaction, or UserLibrary borrow) - i.e. "how was this
     * transaction's royalty distributed". Powers the admin transaction
     * drill-down.
     */
    java.util.List<RoyaltyLedgerResponse> getDistributionForSource(
            RoyaltyLedger.SourceType sourceType, Long sourceReferenceId);

    RoyaltySummaryResponse getSummary(Long beneficiaryId);
}
