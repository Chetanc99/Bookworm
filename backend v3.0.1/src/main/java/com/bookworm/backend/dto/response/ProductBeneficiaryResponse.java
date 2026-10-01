package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductBeneficiaryResponse {
    private Long productBeneficiaryId;
    private Long beneficiaryId;
    private String beneficiaryName;
    private BigDecimal royaltyPercentage;
}
