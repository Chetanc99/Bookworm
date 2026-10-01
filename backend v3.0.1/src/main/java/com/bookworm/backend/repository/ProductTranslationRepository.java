package com.bookworm.backend.repository;

import com.bookworm.backend.entity.ProductTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductTranslationRepository extends JpaRepository<ProductTranslation, Long> {
    List<ProductTranslation> findByProduct_ProductId(Long productId);
    Optional<ProductTranslation> findByProduct_ProductIdAndLanguage_LanguageId(Long productId, Long languageId);
    void deleteByProduct_ProductIdAndLanguage_LanguageId(Long productId, Long languageId);
}
