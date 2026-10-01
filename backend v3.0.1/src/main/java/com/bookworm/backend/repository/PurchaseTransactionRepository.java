package com.bookworm.backend.repository;

import com.bookworm.backend.entity.PurchaseTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    Page<PurchaseTransaction> findByUserId(Long userId, Pageable pageable);
}
