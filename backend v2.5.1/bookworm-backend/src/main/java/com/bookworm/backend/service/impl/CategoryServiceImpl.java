package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.CategoryRequest;
import com.bookworm.backend.dto.response.CategoryResponse;
import com.bookworm.backend.entity.ProductCategory;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.CategoryMapper;
import com.bookworm.backend.repository.ProductCategoryRepository;
import com.bookworm.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // constructor injection via Lombok - no field injection
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllActive() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getById(Long categoryId) {
        ProductCategory category = findEntityOrThrow(categoryId);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new DuplicateResourceException(
                    "Category '" + request.getCategoryName() + "' already exists");
        }
        ProductCategory category = ProductCategory.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long categoryId, CategoryRequest request) {
        ProductCategory category = findEntityOrThrow(categoryId);

        // If the name is changing, re-check uniqueness against other rows.
        if (!category.getCategoryName().equalsIgnoreCase(request.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new DuplicateResourceException(
                    "Category '" + request.getCategoryName() + "' already exists");
        }

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        ProductCategory category = findEntityOrThrow(categoryId);
        // Soft delete: PRODUCT_SUBCATEGORIES has ON DELETE RESTRICT against this table,
        // so a hard delete would throw a DB constraint violation once subcategories exist.
        // is_active=false is the correct semantic delete for reference data anyway.
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private ProductCategory findEntityOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "category_id", categoryId));
    }
}
