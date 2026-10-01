package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.RoyaltyLedgerResponse;
import com.bookworm.backend.entity.RoyaltyLedger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RoyaltyMapper {

    public RoyaltyLedgerResponse toResponse(RoyaltyLedger entity) {
        return RoyaltyLedgerResponse.builder()
                .royaltyLedgerId(entity.getRoyaltyLedgerId())
                .beneficiaryId(entity.getBeneficiary().getBeneficiaryId())
                // Prefer the frozen-at-write-time snapshot so a later beneficiary rename
                // never rewrites history; only rows written before this column existed
                // (null snapshot) fall back to the live name.
                .beneficiaryName(StringUtils.hasText(entity.getBeneficiaryNameSnapshot())
                        ? entity.getBeneficiaryNameSnapshot()
                        : entity.getBeneficiary().getName())
                .productId(entity.getProduct().getProductId())
                .productTitle(entity.getProduct().getTitle())
                .sourceType(entity.getSourceType())
                .sourceReferenceId(entity.getSourceReferenceId())
                .grossAmount(entity.getGrossAmount())
                .royaltyPercentage(entity.getRoyaltyPercentage())
                .royaltyAmount(entity.getRoyaltyAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .paidAt(entity.getPaidAt())
                .build();
    }
}
