package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.BeneficiaryTypeResponse;
import com.bookworm.backend.entity.BeneficiaryType;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryTypeMapper {
    public BeneficiaryTypeResponse toResponse(BeneficiaryType entity) {
        return BeneficiaryTypeResponse.builder()
                .beneficiaryTypeId(entity.getBeneficiaryTypeId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultRoyaltyPercentage(entity.getDefaultRoyaltyPercentage())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
