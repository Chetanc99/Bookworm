package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.CategoryResponse;
import com.bookworm.backend.entity.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(ProductCategory entity) {
        return CategoryResponse.builder()
                .categoryId(entity.getCategoryId())
                .categoryName(entity.getCategoryName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
