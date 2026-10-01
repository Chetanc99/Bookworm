package com.bookworm.backend.repository;

import com.bookworm.backend.entity.ProductSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductSubcategoryRepository extends JpaRepository<ProductSubcategory, Long> {

    List<ProductSubcategory> findByCategory_CategoryIdAndIsActiveTrueOrderBySortOrderAsc(Long categoryId);

    List<ProductSubcategory> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<ProductSubcategory> findBySubcategoryNameIgnoreCaseAndCategory_CategoryId(
            String subcategoryName, Long categoryId);

    boolean existsBySubcategoryNameIgnoreCaseAndCategory_CategoryId(String subcategoryName, Long categoryId);
}
