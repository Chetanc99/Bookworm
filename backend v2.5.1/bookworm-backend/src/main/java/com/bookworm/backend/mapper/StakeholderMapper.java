package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.StakeholderResponse;
import com.bookworm.backend.entity.Stakeholder;
import org.springframework.stereotype.Component;

@Component
public class StakeholderMapper {
    public StakeholderResponse toResponse(Stakeholder entity) {
        return StakeholderResponse.builder()
                .stakeholderId(entity.getStakeholderId())
                .name(entity.getName())
                .type(entity.getType())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
