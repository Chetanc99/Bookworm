package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.BeneficiaryResponse;
import com.bookworm.backend.entity.Beneficiary;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryMapper {
    public BeneficiaryResponse toResponse(Beneficiary entity) {
        return BeneficiaryResponse.builder()
                .beneficiaryId(entity.getBeneficiaryId())
                .name(entity.getName())
                .description(entity.getDescription())
                .beneficiaryTypeId(entity.getBeneficiaryType() != null
                        ? entity.getBeneficiaryType().getBeneficiaryTypeId() : null)
                .beneficiaryTypeName(entity.getBeneficiaryType() != null
                        ? entity.getBeneficiaryType().getName() : null)
                .defaultRoyaltyPercentage(entity.getBeneficiaryType() != null
                        ? entity.getBeneficiaryType().getDefaultRoyaltyPercentage() : null)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
