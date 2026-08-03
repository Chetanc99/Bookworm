package com.bookworm.backend.repository;

import com.bookworm.backend.entity.ProductStakeholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductStakeholderRepository extends JpaRepository<ProductStakeholder, Long> {
    List<ProductStakeholder> findByProduct_ProductId(Long productId);
    boolean existsByProduct_ProductIdAndStakeholder_StakeholderIdAndRole(
            Long productId, Long stakeholderId, String role);
}
