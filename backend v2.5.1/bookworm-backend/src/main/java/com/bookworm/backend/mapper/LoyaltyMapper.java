package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.LoyaltyLedgerResponse;
import com.bookworm.backend.entity.LoyaltyLedger;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyMapper {

    public LoyaltyLedgerResponse toResponse(LoyaltyLedger entity) {
        return LoyaltyLedgerResponse.builder()
                .loyaltyLedgerId(entity.getLoyaltyLedgerId())
                .entryType(entity.getEntryType())
                .points(entity.getPoints())
                .reason(entity.getReason())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
