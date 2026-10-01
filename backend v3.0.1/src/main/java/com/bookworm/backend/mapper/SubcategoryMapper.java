package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.SubcategoryResponse;
import com.bookworm.backend.entity.ProductSubcategory;
import org.springframework.stereotype.Component;

@Component
public class SubcategoryMapper {

    public SubcategoryResponse toResponse(ProductSubcategory entity) {
        return SubcategoryResponse.builder()
                .subcategoryId(entity.getSubcategoryId())
                .categoryId(entity.getCategory().getCategoryId())
                .categoryName(entity.getCategory().getCategoryName())
                .subcategoryName(entity.getSubcategoryName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
