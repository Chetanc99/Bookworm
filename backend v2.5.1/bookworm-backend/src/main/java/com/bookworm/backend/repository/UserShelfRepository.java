package com.bookworm.backend.repository;

import com.bookworm.backend.entity.UserShelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserShelfRepository extends JpaRepository<UserShelf, Long> {
    List<UserShelf> findByUserId(Long userId);
    List<UserShelf> findByUserIdOrderByAcquiredAtDesc(Long userId);
    boolean existsByUserIdAndProduct_ProductIdAndSource(Long userId, Long productId, UserShelf.Source source);
    Optional<UserShelf> findBySourceAndSourceReferenceId(UserShelf.Source source, Long sourceReferenceId);

    // Used by the scheduled cleanup job's shelf-pruning step. PURCHASE rows have
    // expiresAt = null and are never matched here, so they're never pruned.
    List<UserShelf> findByExpiresAtBefore(LocalDateTime now);
}
