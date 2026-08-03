package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;
import com.bookworm.backend.dto.response.ProductResponse;
import com.bookworm.backend.entity.Genre;
import com.bookworm.backend.entity.Product;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class ProductMapper {

    // Convenience for call sites (e.g. browse()) that don't want a per-product
    // beneficiary query - same "no beneficiaries loaded" meaning as an empty list.
    public ProductResponse toResponse(Product entity) {
        return toResponse(entity, List.of());
    }

    // ProductMapper stays dependency-free like every other mapper in this codebase
    // (PurchaseMapper.toResponse(tx, items, points) is the same shape) - the caller
    // assembles the beneficiaries list via ProductBeneficiaryService and hands it in,
    // rather than this mapper reaching into a repository itself.
    public ProductResponse toResponse(Product entity, List<ProductBeneficiaryResponse> beneficiaries) {
        Genre genre = entity.getGenre();
        return ProductResponse.builder()
                .productId(entity.getProductId())
                .subcategoryId(entity.getSubcategory().getSubcategoryId())
                .subcategoryName(entity.getSubcategory().getSubcategoryName())
                .genreId(genre != null ? genre.getGenreId() : null)
                .genreName(genre != null ? genre.getGenreName() : null)
                .languageId(entity.getLanguage().getLanguageId())
                .languageName(entity.getLanguage().getLanguageName())
                .title(entity.getTitle())
                .shortDescription(entity.getShortDescription())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .pages(entity.getPages())
                .duration(entity.getDuration())
                .coverImage(entity.getCoverImage())
                .fileType(entity.getFileType())
                // file_path intentionally excluded - it's the raw content location and must only
                // ever be resolved server-side after ownership/rental/library access is verified,
                // never handed to the client on a catalog browse response. hasContentFile exposes
                // just the presence/absence, so admin UIs can still show accurate status.
                .hasContentFile(StringUtils.hasText(entity.getFilePath()))
                .isRentable(entity.getIsRentable())
                .isLibraryEligible(entity.getIsLibraryEligible())
                .rentRate(entity.getRentRate())
                .minRentDays(entity.getMinRentDays())
                .isAvailable(entity.getIsAvailable())
                .beneficiaries(beneficiaries)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
