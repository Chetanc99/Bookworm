package com.bookworm.backend.repository;

import com.bookworm.backend.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    List<PurchaseItem> findByPurchaseTransaction_PurchaseTransactionId(Long purchaseTransactionId);
}
