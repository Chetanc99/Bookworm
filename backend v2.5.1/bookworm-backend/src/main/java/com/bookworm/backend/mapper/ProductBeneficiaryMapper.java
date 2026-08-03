package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;
import com.bookworm.backend.entity.ProductBeneficiary;
import org.springframework.stereotype.Component;

@Component
public class ProductBeneficiaryMapper {
    public ProductBeneficiaryResponse toResponse(ProductBeneficiary entity) {
        return ProductBeneficiaryResponse.builder()
                .productBeneficiaryId(entity.getProductBeneficiaryId())
                .beneficiaryId(entity.getBeneficiary().getBeneficiaryId())
                .beneficiaryName(entity.getBeneficiary().getName())
                .royaltyPercentage(entity.getRoyaltyPercentage())
                .build();
    }
}
