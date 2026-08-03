package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.ProductStakeholderResponse;
import com.bookworm.backend.entity.ProductStakeholder;
import org.springframework.stereotype.Component;

@Component
public class ProductStakeholderMapper {
    public ProductStakeholderResponse toResponse(ProductStakeholder entity) {
        return ProductStakeholderResponse.builder()
                .productStakeholderId(entity.getProductStakeholderId())
                .stakeholderId(entity.getStakeholder().getStakeholderId())
                .stakeholderName(entity.getStakeholder().getName())
                .stakeholderType(entity.getStakeholder().getType())
                .role(entity.getRole())
                .build();
    }
}
