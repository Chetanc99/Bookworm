package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.ProductResponse;
import com.bookworm.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    /**
     * mediaType filters the catalog by BOOK/AUDIOBOOK/VIDEO_COURSE/PODCAST
     * (null = all types). displayLanguageId, if provided and a
     * ProductTranslation exists for a given product in that language,
     * overlays the translated title/shortDescription/description onto the
     * response in place of the product's base-language text - the
     * underlying Product row (price, availability, etc.) is unaffected.
     */
    Page<ProductResponse> browse(Long subcategoryId, Long genreId, Long languageId, Boolean isRentable,
                                  Product.MediaType mediaType, String keyword, Long displayLanguageId, Pageable pageable);
    ProductResponse getById(Long productId, Long displayLanguageId);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long productId, ProductRequest request);
    void delete(Long productId);
}
