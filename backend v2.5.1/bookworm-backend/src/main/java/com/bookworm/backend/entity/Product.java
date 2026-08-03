package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deliberately has no @OneToMany back to ProductBeneficiary (unlike the
 * temptation to add one for "a product's beneficiaries"). Every other
 * reference association in this codebase (Genre, Language, Subcategory) is
 * unidirectional FK-only for the same reasons: it avoids Jackson
 * serialization cycles on any endpoint that returns a raw Product, avoids
 * LazyInitializationException outside a transaction, and keeps write/read
 * for splits going through one place. ProductBeneficiaryRepository /
 * ProductBeneficiaryService.getByProduct() is the single source of truth for
 * "this product's beneficiaries" - ProductServiceImpl assembles
 * ProductResponse.beneficiaries from it explicitly rather than traversing an
 * entity graph.
 */
@Entity
@Table(name = "PRODUCTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private ProductSubcategory subcategory;

    // Nullable in the schema - not every product (e.g. some video courses) has a genre.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "is_rentable", nullable = false)
    @Builder.Default
    private Boolean isRentable = false;

    @Column(name = "is_library_eligible", nullable = false)
    @Builder.Default
    private Boolean isLibraryEligible = false;

    @Column(name = "rent_rate", precision = 12, scale = 2)
    private BigDecimal rentRate;

    @Column(name = "min_rent_days")
    private Integer minRentDays;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

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
