package com.bookworm.backend.repository;

import com.bookworm.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Bilingual search: keyword matches either the product's own (base-language)
    // title/description OR any PRODUCT_TRANSLATIONS row for it - so searching
    // in Devanagari finds a product whose base title is English (and has a
    // Marathi/Hindi translation) and vice versa, without duplicating the
    // catalog row per language. LEFT JOIN (not INNER) so products with no
    // translations at all are still matched by their base title as before;
    // DISTINCT avoids one product appearing twice when more than one of its
    // translations happens to match the same keyword.
    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN ProductTranslation pt ON pt.product = p
            WHERE p.isAvailable = true
            AND (:subcategoryId IS NULL OR p.subcategory.subcategoryId = :subcategoryId)
            AND (:genreId IS NULL OR p.genre.genreId = :genreId)
            AND (:languageId IS NULL OR p.language.languageId = :languageId)
            AND (:isRentable IS NULL OR p.isRentable = :isRentable)
            AND (:mediaType IS NULL OR p.mediaType = :mediaType)
            AND (:keyword IS NULL
                 OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(pt.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> filterProducts(
            @Param("subcategoryId") Long subcategoryId,
            @Param("genreId") Long genreId,
            @Param("languageId") Long languageId,
            @Param("isRentable") Boolean isRentable,
            @Param("mediaType") Product.MediaType mediaType,
            @Param("keyword") String keyword,
            Pageable pageable);
}
