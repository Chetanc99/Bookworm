package com.bookworm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * An alternate-language display overlay for a Product's title/short
 * description/description - NOT a separate catalog entry. A product keeps
 * exactly one price, one set of purchase/rent/library-eligibility settings,
 * and one royalty configuration regardless of how many languages it has
 * display text in; only the text a customer reads changes per translation.
 *
 * This is deliberately a separate table rather than extra nullable columns
 * directly on PRODUCTS (title_hi, title_mr, title_en, ...) - that approach
 * doesn't scale past a fixed, hardcoded set of languages, while this table
 * supports however many languages LANGUAGES already has, with zero schema
 * change to add a new one (same "reference data lives in the DB, not in
 * code" convention as Language/Category/BeneficiaryType).
 *
 * One row per (product, language) - see the unique constraint below.
 * Product.language (the base FK) remains the product's "native"/original
 * language and is unaffected by translations; a translation is only
 * consulted when a caller explicitly asks to view the product in some other
 * language (ProductServiceImpl's displayLanguageId parameter).
 */
@Entity
@Table(name = "PRODUCT_TRANSLATIONS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "language_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_translation_id")
    private Long productTranslationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
