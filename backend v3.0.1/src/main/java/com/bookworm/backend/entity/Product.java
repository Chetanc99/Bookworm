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

    /**
     * Extends the catalog beyond books without a separate table per type -
     * every other column (pages/duration/fileType/filePath/rentRate/
     * isLibraryEligible/UserShelf/Cart/PurchaseItem/RentTransaction/
     * UserLibrary/RoyaltyLedger) already works generically per-product
     * regardless of what kind of content it is, so the only genuinely new
     * piece of information a media type needs is "which kind is this" for
     * filtering/display/upload-validation purposes. duration already covers
     * AUDIOBOOK/VIDEO_COURSE run time and pages already covers BOOK length;
     * a PODCAST's "duration" is interpreted as per-episode length with
     * episodeCount tracking how many episodes, avoiding yet another set of
     * type-specific columns for a fourth type that's structurally identical
     * to an audiobook otherwise.
     */
    public enum MediaType {
        BOOK, AUDIOBOOK, VIDEO_COURSE, PODCAST
    }

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

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    @Builder.Default
    private MediaType mediaType = MediaType.BOOK;

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

    // Only meaningful when mediaType = PODCAST; null for every other type.
    // Duration (existing column above) is interpreted as per-episode length
    // for podcasts rather than adding a separate "episode duration" column.
    @Column(name = "episode_count")
    private Integer episodeCount;

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
