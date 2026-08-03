package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.CategoryRequest;
import com.bookworm.backend.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllActive();
    List<CategoryResponse> getAll();
    CategoryResponse getById(Long categoryId);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long categoryId, CategoryRequest request);
    void delete(Long categoryId);
}
