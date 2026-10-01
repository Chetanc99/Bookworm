package com.bookworm.backend.repository;

import com.bookworm.backend.entity.RentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RentTransactionRepository extends JpaRepository<RentTransaction, Long> {
    Page<RentTransaction> findByUserId(Long userId, Pageable pageable);

    // Used by the (future) scheduled job that flips ACTIVE -> EXPIRED and prunes the shelf.
    List<RentTransaction> findByStatusAndEndDateBefore(RentTransaction.Status status, LocalDateTime now);
}
