package com.bookworm.backend.repository;

import com.bookworm.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    // Invalidate any earlier outstanding tokens when a new reset is requested.
    void deleteByUserId(Long userId);
}
