package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.SubcategoryRequest;
import com.bookworm.backend.dto.response.SubcategoryResponse;

import java.util.List;

public interface SubcategoryService {
    List<SubcategoryResponse> getAll();
    List<SubcategoryResponse> getByCategory(Long categoryId);
    SubcategoryResponse getById(Long subcategoryId);
    SubcategoryResponse create(SubcategoryRequest request);
    SubcategoryResponse update(Long subcategoryId, SubcategoryRequest request);
    void delete(Long subcategoryId);
}
