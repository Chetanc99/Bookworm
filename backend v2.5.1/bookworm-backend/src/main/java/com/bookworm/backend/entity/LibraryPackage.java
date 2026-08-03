package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A subscription tier (e.g. "Basic Library" - 30 days, 3 concurrent
 * borrows). Admin-managed reference data, same soft-delete reasoning as
 * Category/Genre/Language: USER_LIBRARY_PACKAGES references this, so a
 * hard delete would break FK once anyone has subscribed.
 */
@Entity
@Table(name = "LIBRARY_PACKAGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_package_id")
    private Long libraryPackageId;

    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    // Cap on how many products a subscriber can have simultaneously borrowed under this tier.
    @Column(name = "max_concurrent_borrows", nullable = false)
    @Builder.Default
    private Integer maxConcurrentBorrows = 3;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
