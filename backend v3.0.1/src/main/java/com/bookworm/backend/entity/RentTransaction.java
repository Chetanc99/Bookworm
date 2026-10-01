package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single rented product with its own validity window. Unlike Purchase
 * (which batches all cart items into one transaction with N line items),
 * each rented product gets its own RentTransaction row - rentDays/rentRate
 * are per-product and the expiry clock is per-product, so there's no
 * natural "parent" transaction to batch them under. rentRate is a
 * snapshot, same reasoning as PurchaseItem.unitPrice.
 */
@Entity
@Table(name = "RENT_TRANSACTIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentTransaction {

    public enum Status {
        ACTIVE, EXPIRED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rent_transaction_id")
    private Long rentTransactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "rent_days", nullable = false)
    private Integer rentDays;

    @Column(name = "rent_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentRate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
