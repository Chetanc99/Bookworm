package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long productId;

    private Long subcategoryId;
    private String subcategoryName;
    private Long genreId;
    private String genreName;
    private Long languageId;
    private String languageName;

    private com.bookworm.backend.entity.Product.MediaType mediaType;
    private Integer episodeCount;

    private String title;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private Integer pages;
    private Integer duration;
    private String coverImage;
    private String fileType;
    // Deliberately a boolean, not the raw filePath itself (see ProductMapper) -
    // lets admin UIs (Manage Products) show "content attached?" status without
    // exposing the actual content location on a response every authenticated
    // user can request via GET /products/{id}.
    private boolean hasContentFile;

    private Boolean isRentable;
    private Boolean isLibraryEligible;
    private BigDecimal rentRate;
    private Integer minRentDays;
    private Boolean isAvailable;

    // The product's current royalty allocation - see ProductBeneficiaryService,
    // the single source of truth for these. Never touches RoyaltyLedger's own
    // frozen snapshots; this reflects live, editable configuration only.
    private List<ProductBeneficiaryResponse> beneficiaries;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
