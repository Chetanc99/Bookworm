package com.bookworm.backend.repository;

import com.bookworm.backend.entity.LoyaltyLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedger, Long> {
    List<LoyaltyLedger> findByUserId(Long userId);
    Page<LoyaltyLedger> findByUserId(Long userId, Pageable pageable);
    Optional<LoyaltyLedger> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
