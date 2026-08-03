package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> browse(Long subcategoryId, Long genreId, Long languageId, Boolean isRentable, String keyword, Pageable pageable);
    ProductResponse getById(Long productId);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long productId, ProductRequest request);
    void delete(Long productId);
}
