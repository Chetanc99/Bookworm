package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One borrowed product under a subscription - the "lend" side of
 * rent/lend. Bounded by both its own borrowDays window and the parent
 * subscription's end_date (whichever comes first), enforced in the
 * service layer at borrow time.
 */
@Entity
@Table(name = "USER_LIBRARY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLibrary {

    public enum Status {
        BORROWED, RETURNED, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_library_id")
    private Long userLibraryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_library_package_id", nullable = false)
    private UserLibraryPackage userLibraryPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "borrowed_at", nullable = false, updatable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.BORROWED;

    @PrePersist
    protected void onCreate() {
        borrowedAt = LocalDateTime.now();
    }
}
