package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Append-only royalty accrual, one row per (beneficiary, revenue event).
 * royaltyPercentage is snapshotted from ProductBeneficiary at the moment of
 * the sale/rental/borrow - if the split changes later, past accruals must
 * not silently change. sourceType + sourceReferenceId trace back to the
 * PurchaseItem/RentTransaction/UserLibrary row that generated it, same
 * pattern as LoyaltyLedger's referenceType/referenceId.
 *
 * status is the only concession made now toward future payout support: it
 * lets a later RoyaltyPayoutService mark rows PAID (or REVERSED for a future
 * refund/adjustment flow) without any other schema change. Nothing in this
 * codebase transitions status yet - every row is written UNPAID and stays
 * that way until a payout feature exists to move it.
 *
 * beneficiaryNameSnapshot: royaltyPercentage was already a snapshot (Beneficiary
 * and ProductBeneficiary can both change after the fact without touching past
 * rows), but the beneficiary's *name* was only ever read live through the FK -
 * renaming a beneficiary would silently rewrite how every past ledger row
 * displays. This column freezes the name at write time so historical reports
 * are immune to a later rename, same as royaltyPercentage already is to a
 * later split change. Nullable because rows written before this column
 * existed have nothing to backfill it with - RoyaltyMapper falls back to the
 * live beneficiary.name for those, which is the best available answer for
 * data that predates this guarantee.
 */
@Entity
@Table(name = "ROYALTY_LEDGER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoyaltyLedger {

    public enum SourceType {
        PURCHASE, RENT, LIBRARY
    }

    public enum PayoutStatus {
        UNPAID, PAID, REVERSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "royalty_ledger_id")
    private Long royaltyLedgerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    // Frozen at write time - see class javadoc. Never updated after insert.
    @Column(name = "beneficiary_name_snapshot", length = 150)
    private String beneficiaryNameSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "source_reference_id", nullable = false)
    private Long sourceReferenceId;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "royalty_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal royaltyPercentage;

    @Column(name = "royalty_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal royaltyAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'UNPAID'")
    @Builder.Default
    private PayoutStatus status = PayoutStatus.UNPAID;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // When status transitioned to PAID (see RoyaltyServiceImpl.markBeneficiaryRoyaltiesPaid).
    // Null for UNPAID/REVERSED rows and for any PAID row written before this
    // column existed. This is the ONLY field a payout ever touches on an
    // existing row - royaltyAmount/royaltyPercentage/beneficiaryNameSnapshot/
    // grossAmount are never rewritten, preserving the append-only historical
    // guarantee the rest of this entity's javadoc describes. Marking paid is a
    // status transition on an existing fact ("this was paid on date X"), not a
    // correction of what was earned.
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
