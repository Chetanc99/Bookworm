package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Append-only trail of admin-initiated changes (role changes, account
 * activate/deactivate, product create/update/delete, bulk import runs).
 * Deliberately a plain flat table, not tied via FK to USERS - actorUserId is
 * a plain Long (same convention as LoyaltyLedger/UserShelf/PasswordResetToken)
 * so a later user deletion never blocks on this table, and the row still
 * carries actorEmail as a readable snapshot even if the actor account is
 * later removed/renamed.
 */
@Entity
@Table(name = "AUDIT_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long auditLogId;

    @Column(name = "actor_user_id")
    private Long actorUserId; // null if the action wasn't attributable to an authenticated principal

    @Column(name = "actor_email", length = 150)
    private String actorEmail;

    // e.g. "ROLE_CHANGE", "ACTIVATE", "DEACTIVATE", "PRODUCT_CREATE",
    // "PRODUCT_UPDATE", "PRODUCT_DELETE", "BULK_IMPORT"
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    // e.g. "USER", "PRODUCT", "LIBRARY_PACKAGE"
    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
