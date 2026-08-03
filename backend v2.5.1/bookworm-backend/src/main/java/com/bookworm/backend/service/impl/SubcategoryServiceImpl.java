package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.SubcategoryRequest;
import com.bookworm.backend.dto.response.SubcategoryResponse;
import com.bookworm.backend.entity.ProductCategory;
import com.bookworm.backend.entity.ProductSubcategory;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.SubcategoryMapper;
import com.bookworm.backend.repository.ProductCategoryRepository;
import com.bookworm.backend.repository.ProductSubcategoryRepository;
import com.bookworm.backend.service.SubcategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubcategoryServiceImpl implements SubcategoryService {

    private final ProductSubcategoryRepository subcategoryRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SubcategoryMapper subcategoryMapper;

    @Override
    public List<SubcategoryResponse> getAll() {
        return subcategoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream().map(subcategoryMapper::toResponse).toList();
    }

    @Override
    public List<SubcategoryResponse> getByCategory(Long categoryId) {
        return subcategoryRepository.findByCategory_CategoryIdAndIsActiveTrueOrderBySortOrderAsc(categoryId)
                .stream().map(subcategoryMapper::toResponse).toList();
    }

    @Override
    public SubcategoryResponse getById(Long subcategoryId) {
        return subcategoryMapper.toResponse(findEntityOrThrow(subcategoryId));
    }

    @Override
    @Transactional
    public SubcategoryResponse create(SubcategoryRequest request) {
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "category_id", request.getCategoryId()));

        if (subcategoryRepository.existsBySubcategoryNameIgnoreCaseAndCategory_CategoryId(
                request.getSubcategoryName(), request.getCategoryId())) {
            throw new DuplicateResourceException(
                    "Subcategory '" + request.getSubcategoryName() + "' already exists under this category");
        }

        ProductSubcategory subcategory = ProductSubcategory.builder()
                .category(category)
                .subcategoryName(request.getSubcategoryName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return subcategoryMapper.toResponse(subcategoryRepository.save(subcategory));
    }

    @Override
    @Transactional
    public SubcategoryResponse update(Long subcategoryId, SubcategoryRequest request) {
        ProductSubcategory subcategory = findEntityOrThrow(subcategoryId);

        // Allow re-parenting to a different category.
        if (!subcategory.getCategory().getCategoryId().equals(request.getCategoryId())) {
            ProductCategory newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "category_id", request.getCategoryId()));
            subcategory.setCategory(newCategory);
        }

        subcategory.setSubcategoryName(request.getSubcategoryName());
        subcategory.setDescription(request.getDescription());
        if (request.getSortOrder() != null) subcategory.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) subcategory.setIsActive(request.getIsActive());

        return subcategoryMapper.toResponse(subcategoryRepository.save(subcategory));
    }

    @Override
    @Transactional
    public void delete(Long subcategoryId) {
        ProductSubcategory subcategory = findEntityOrThrow(subcategoryId);
        // Soft delete only - GENRES and PRODUCTS both have ON DELETE RESTRICT on this table.
        subcategory.setIsActive(false);
        subcategoryRepository.save(subcategory);
    }

    private ProductSubcategory findEntityOrThrow(Long subcategoryId) {
        return subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategory_id", subcategoryId));
    }
}
