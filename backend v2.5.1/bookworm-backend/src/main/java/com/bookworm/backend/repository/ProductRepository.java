package com.bookworm.backend.repository;

import com.bookworm.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p
            WHERE p.isAvailable = true
            AND (:subcategoryId IS NULL OR p.subcategory.subcategoryId = :subcategoryId)
            AND (:genreId IS NULL OR p.genre.genreId = :genreId)
            AND (:languageId IS NULL OR p.language.languageId = :languageId)
            AND (:isRentable IS NULL OR p.isRentable = :isRentable)
            AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> filterProducts(
            @Param("subcategoryId") Long subcategoryId,
            @Param("genreId") Long genreId,
            @Param("languageId") Long languageId,
            @Param("isRentable") Boolean isRentable,
            @Param("keyword") String keyword,
            Pageable pageable);
}
