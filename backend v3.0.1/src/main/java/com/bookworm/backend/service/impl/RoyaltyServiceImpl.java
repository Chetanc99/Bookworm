package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.RoyaltyLedgerResponse;
import com.bookworm.backend.dto.response.RoyaltySummaryResponse;
import com.bookworm.backend.entity.Beneficiary;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductBeneficiary;
import com.bookworm.backend.entity.RoyaltyLedger;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.RoyaltyMapper;
import com.bookworm.backend.repository.BeneficiaryRepository;
import com.bookworm.backend.repository.ProductBeneficiaryRepository;
import com.bookworm.backend.repository.RoyaltyLedgerRepository;
import com.bookworm.backend.service.RoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reads ProductBeneficiary for the product being sold/rented/borrowed and
 * writes one RoyaltyLedger row per configured beneficiary, split via
 * RoyaltySplitCalculator so the rows always sum exactly to the royalty
 * total for that event. If royalty percentages for a product don't sum to
 * 100, that's a data-entry problem in the PRODUCT_BENEFICIARIES admin
 * screen, not something this service corrects - it splits exactly what's
 * configured, whatever that sums to.
 *
 * Responsibilities are deliberately kept separate: RoyaltySplitCalculator
 * does the (pure, dependency-free) percentage math; this class resolves who
 * gets paid and persists the ledger; RoyaltyLedger.status is the seam a
 * future RoyaltyPayoutService would use to mark rows PAID without touching
 * either of the above.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoyaltyServiceImpl implements RoyaltyService {

    // Historical default for products with no PRODUCT_BENEFICIARIES row at all -
    // predates BeneficiaryType presets, kept as-is so already-live products
    // without an explicit split keep accruing exactly what they always have.
    private static final BigDecimal DEFAULT_FALLBACK_PERCENT = new BigDecimal("10.00");

    private final ProductBeneficiaryRepository productBeneficiaryRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final RoyaltyLedgerRepository royaltyLedgerRepository;
    private final RoyaltySplitCalculator splitCalculator;
    private final RoyaltyMapper mapper;

    @Override
    @Transactional
    public void recordForProduct(Product product, BigDecimal grossAmount,
                                  RoyaltyLedger.SourceType sourceType, Long sourceReferenceId) {
        List<ProductBeneficiary> splits = productBeneficiaryRepository.findByProduct_ProductId(product.getProductId());

        List<RoyaltySplitCalculator.Share<Beneficiary>> shares;
        if (!splits.isEmpty()) {
            shares = splits.stream()
                    .map(s -> new RoyaltySplitCalculator.Share<>(s.getBeneficiary(), s.getRoyaltyPercentage()))
                    .toList();
        } else {
            List<Beneficiary> allBens = beneficiaryRepository.findAll();
            if (allBens.isEmpty()) return;
            Beneficiary ben = allBens.stream()
                    .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                    .findFirst().orElse(allBens.get(0));
            shares = List.of(new RoyaltySplitCalculator.Share<>(ben, DEFAULT_FALLBACK_PERCENT));
        }

        List<RoyaltySplitCalculator.Allocation<Beneficiary>> allocations = splitCalculator.split(grossAmount, shares);

        for (RoyaltySplitCalculator.Allocation<Beneficiary> allocation : allocations) {
            royaltyLedgerRepository.save(RoyaltyLedger.builder()
                    .beneficiary(allocation.key())
                    // Frozen now, at the moment this royalty is earned - a later rename
                    // of the Beneficiary must never change what this row reports.
                    .beneficiaryNameSnapshot(allocation.key().getName())
                    .product(product)
                    .sourceType(sourceType)
                    .sourceReferenceId(sourceReferenceId)
                    .grossAmount(grossAmount)
                    .royaltyPercentage(allocation.percentage())
                    .royaltyAmount(allocation.amount())
                    .status(RoyaltyLedger.PayoutStatus.UNPAID)
                    .build());
        }
    }

    @Override
    public Page<RoyaltyLedgerResponse> getHistory(Long beneficiaryId, Pageable pageable) {
        return royaltyLedgerRepository.findByBeneficiary_BeneficiaryId(beneficiaryId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public List<RoyaltyLedgerResponse> getDistributionForSource(
            RoyaltyLedger.SourceType sourceType, Long sourceReferenceId) {
        return royaltyLedgerRepository.findBySourceTypeAndSourceReferenceId(sourceType, sourceReferenceId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public RoyaltySummaryResponse getSummary(Long beneficiaryId) {
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary", "beneficiary_id", beneficiaryId));

        BigDecimal total = royaltyLedgerRepository.sumRoyaltyAmountByBeneficiary(beneficiaryId);
        BigDecimal unpaid = royaltyLedgerRepository.sumRoyaltyAmountByBeneficiaryAndStatus(
                beneficiaryId, RoyaltyLedger.PayoutStatus.UNPAID);
        BigDecimal paid = royaltyLedgerRepository.sumRoyaltyAmountByBeneficiaryAndStatus(
                beneficiaryId, RoyaltyLedger.PayoutStatus.PAID);
        return RoyaltySummaryResponse.builder()
                .beneficiaryId(beneficiary.getBeneficiaryId())
                .beneficiaryName(beneficiary.getName())
                .totalRoyaltyEarned(total)
                .unpaidRoyalty(unpaid)
                .paidRoyalty(paid)
                .build();
    }
}
