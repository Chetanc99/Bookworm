package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductTranslationRequest;
import com.bookworm.backend.dto.response.ProductTranslationResponse;

import java.util.List;

public interface ProductTranslationService {

    List<ProductTranslationResponse> getByProduct(Long productId);

    /** Upsert - one row per (product, language); calling again for the same language replaces its text. */
    ProductTranslationResponse upsert(Long productId, ProductTranslationRequest request);

    void remove(Long productId, Long languageId);
}
