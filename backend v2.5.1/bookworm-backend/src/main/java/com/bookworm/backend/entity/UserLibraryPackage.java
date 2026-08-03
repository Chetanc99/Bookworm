package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One purchased subscription window. A user can hold at most one ACTIVE
 * subscription at a time (enforced in the service layer, not a DB
 * constraint, since past CANCELLED/EXPIRED rows must remain queryable).
 */
@Entity
@Table(name = "USER_LIBRARY_PACKAGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLibraryPackage {

    public enum Status {
        ACTIVE, EXPIRED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_library_package_id")
    private Long userLibraryPackageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_package_id", nullable = false)
    private LibraryPackage libraryPackage;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        purchasedAt = LocalDateTime.now();
    }
}
