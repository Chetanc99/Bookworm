package com.bookworm.backend.repository;

import com.bookworm.backend.entity.ProductCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCreditRepository extends JpaRepository<ProductCredit, Long> {
    List<ProductCredit> findByProduct_ProductId(Long productId);
}
