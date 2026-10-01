package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Append-only ledger of loyalty point movements. Only EARN entries are
 * written by the Purchase flow for now; REDEEM will come once a spend
 * mechanism exists. Balance is derived by summing points, never stored -
 * avoids a cached total drifting from the ledger.
 */
@Entity
@Table(name = "LOYALTY_LEDGER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyLedger {

    public enum EntryType {
        EARN, REDEEM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loyalty_ledger_id")
    private Long loyaltyLedgerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private EntryType entryType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
