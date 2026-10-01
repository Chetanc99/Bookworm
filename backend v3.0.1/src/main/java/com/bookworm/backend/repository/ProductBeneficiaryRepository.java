package com.bookworm.backend.repository;

import com.bookworm.backend.entity.ProductBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductBeneficiaryRepository extends JpaRepository<ProductBeneficiary, Long> {

    List<ProductBeneficiary> findByProduct_ProductId(Long productId);

    @Query("""
            SELECT COALESCE(SUM(pb.royaltyPercentage), 0) FROM ProductBeneficiary pb
            WHERE pb.product.productId = :productId
            """)
    BigDecimal sumRoyaltyPercentageByProduct(@Param("productId") Long productId);

    boolean existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(Long productId, Long beneficiaryId);
}
