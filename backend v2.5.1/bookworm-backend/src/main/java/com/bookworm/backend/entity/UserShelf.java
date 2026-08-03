package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * "My Shelf" - what a user can currently open/read/play. A purchase grants
 * a permanent entry (expires_at null); Rent and Library flows (built later)
 * will insert their own rows here with an expires_at, and a scheduled
 * cleanup removes expired ones. source + source_reference_id trace back to
 * the transaction/loan that granted access, for support/debugging.
 */
@Entity
@Table(name = "USER_SHELF")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShelf {

    public enum Source {
        PURCHASE, RENT, LIBRARY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_shelf_id")
    private Long userShelfId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private Source source;

    @Column(name = "source_reference_id")
    private Long sourceReferenceId;

    // Null for PURCHASE (permanent access). Populated by Rent/Library flows.
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @PrePersist
    protected void onCreate() {
        acquiredAt = LocalDateTime.now();
    }
}
