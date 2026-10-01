package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.PurchaseItemResponse;
import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.PurchaseItem;
import com.bookworm.backend.entity.PurchaseTransaction;
import com.bookworm.backend.repository.LanguageRepository;
import com.bookworm.backend.repository.ProductTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PurchaseMapper {

    private final LanguageRepository languageRepository;
    private final ProductTranslationRepository productTranslationRepository;

    public PurchaseItemResponse toItemResponse(PurchaseItem entity) {
        Product product = entity.getProduct();
        String languageName = product.getLanguage() != null ? product.getLanguage().getLanguageName() : null;

        // Only look up an English translation when the product's own language
        // isn't already English - see PurchaseItemResponse javadoc/#20.
        String englishTitle = null;
        if (languageName != null && !"english".equalsIgnoreCase(languageName)) {
            englishTitle = languageRepository.findByLanguageNameIgnoreCase("English")
                    .flatMap(english -> productTranslationRepository
                            .findByProduct_ProductIdAndLanguage_LanguageId(product.getProductId(), english.getLanguageId()))
                    .map(com.bookworm.backend.entity.ProductTranslation::getTitle)
                    .orElse(null);
        }

        return PurchaseItemResponse.builder()
                .purchaseItemId(entity.getPurchaseItemId())
                .productId(product.getProductId())
                .productTitle(product.getTitle())
                .unitPrice(entity.getUnitPrice())
                .productLanguageName(languageName)
                .englishTitle(englishTitle)
                .build();
    }

    public PurchaseTransactionResponse toResponse(
            PurchaseTransaction entity, List<PurchaseItem> items, int loyaltyPointsEarned) {
        return PurchaseTransactionResponse.builder()
                .purchaseTransactionId(entity.getPurchaseTransactionId())
                .userId(entity.getUserId())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .loyaltyPointsEarned(loyaltyPointsEarned)
                .items(items.stream().map(this::toItemResponse).toList())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
