package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Single-use, expiring token for the email-verification flow. Kept as a
 * separate entity/table from PasswordResetToken rather than a shared
 * "purpose" enum on one table - same reasoning as PurchaseTransaction vs
 * RentTransaction being separate: different lifecycle, different callers,
 * clearer to read as two small tables than one overloaded one.
 */
@Entity
@Table(name = "EMAIL_VERIFICATION_TOKENS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verification_token_id")
    private Long emailVerificationTokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
