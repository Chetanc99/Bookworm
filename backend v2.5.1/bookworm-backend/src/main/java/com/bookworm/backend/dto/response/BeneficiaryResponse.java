package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BeneficiaryResponse {
    private Long beneficiaryId;
    private String name;
    private String description;
    private Long beneficiaryTypeId;
    private String beneficiaryTypeName;
    // Convenience for the admin UI to pre-fill a percentage before addSplit() -
    // null when this beneficiary has no type.
    private BigDecimal defaultRoyaltyPercentage;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
