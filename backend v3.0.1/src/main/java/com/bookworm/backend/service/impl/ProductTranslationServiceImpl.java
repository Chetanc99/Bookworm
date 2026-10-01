package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductTranslationRequest;
import com.bookworm.backend.dto.response.ProductTranslationResponse;
import com.bookworm.backend.entity.Language;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductTranslation;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.repository.LanguageRepository;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.repository.ProductTranslationRepository;
import com.bookworm.backend.service.ProductTranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTranslationServiceImpl implements ProductTranslationService {

    private final ProductTranslationRepository translationRepository;
    private final ProductRepository productRepository;
    private final LanguageRepository languageRepository;

    @Override
    public List<ProductTranslationResponse> getByProduct(Long productId) {
        return translationRepository.findByProduct_ProductId(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductTranslationResponse upsert(Long productId, ProductTranslationRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language", "language_id", request.getLanguageId()));

        ProductTranslation translation = translationRepository
                .findByProduct_ProductIdAndLanguage_LanguageId(productId, request.getLanguageId())
                .orElseGet(() -> ProductTranslation.builder().product(product).language(language).build());

        translation.setTitle(request.getTitle());
        translation.setShortDescription(request.getShortDescription());
        translation.setDescription(request.getDescription());

        return toResponse(translationRepository.save(translation));
    }

    @Override
    @Transactional
    public void remove(Long productId, Long languageId) {
        translationRepository.deleteByProduct_ProductIdAndLanguage_LanguageId(productId, languageId);
    }

    private ProductTranslationResponse toResponse(ProductTranslation t) {
        return ProductTranslationResponse.builder()
                .productTranslationId(t.getProductTranslationId())
                .languageId(t.getLanguage().getLanguageId())
                .languageName(t.getLanguage().getLanguageName())
                .title(t.getTitle())
                .shortDescription(t.getShortDescription())
                .description(t.getDescription())
                .build();
    }
}
