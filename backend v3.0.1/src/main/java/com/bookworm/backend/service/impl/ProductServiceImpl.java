package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.ProductResponse;
import com.bookworm.backend.entity.Genre;
import com.bookworm.backend.entity.Language;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductSubcategory;
import com.bookworm.backend.entity.ProductTranslation;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.ProductMapper;
import com.bookworm.backend.entity.ProductBeneficiary;
import com.bookworm.backend.repository.BeneficiaryRepository;
import com.bookworm.backend.repository.GenreRepository;
import com.bookworm.backend.repository.LanguageRepository;
import com.bookworm.backend.repository.ProductBeneficiaryRepository;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.repository.ProductSubcategoryRepository;
import com.bookworm.backend.repository.ProductTranslationRepository;
import com.bookworm.backend.service.AuditService;
import com.bookworm.backend.service.ProductBeneficiaryService;
import com.bookworm.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSubcategoryRepository subcategoryRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ProductBeneficiaryRepository productBeneficiaryRepository;
    private final ProductBeneficiaryService productBeneficiaryService;
    private final ProductTranslationRepository productTranslationRepository;
    private final AuditService auditService;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> browse(Long subcategoryId, Long genreId, Long languageId, Boolean isRentable,
                                         Product.MediaType mediaType, String keyword, Long displayLanguageId,
                                         Pageable pageable) {
        String cleanKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Product> page = productRepository.filterProducts(
                subcategoryId, genreId, languageId, isRentable, mediaType, cleanKeyword, pageable);
        return page.map(p -> toResponseWithBeneficiaries(p, displayLanguageId));
    }

    @Override
    public ProductResponse getById(Long productId, Long displayLanguageId) {
        return toResponseWithBeneficiaries(findEntityOrThrow(productId), displayLanguageId);
    }

    private ProductResponse toResponseWithBeneficiaries(Product product, Long displayLanguageId) {
        ProductResponse response = productMapper.toResponse(
                product, productBeneficiaryService.getByProduct(product.getProductId()));
        return overlayTranslation(response, product.getProductId(), displayLanguageId);
    }

    // Overlays a ProductTranslation's title/shortDescription/description onto
    // an already-built response, in place, if the caller asked for a display
    // language AND a translation exists for it. Falls through silently (base
    // text unchanged) if displayLanguageId is null, matches the product's own
    // base language (nothing to overlay), or has no translation row - a
    // missing translation is not an error, just "show the original text."
    private ProductResponse overlayTranslation(ProductResponse response, Long productId, Long displayLanguageId) {
        if (displayLanguageId == null || displayLanguageId.equals(response.getLanguageId())) {
            return response;
        }
        return productTranslationRepository.findByProduct_ProductIdAndLanguage_LanguageId(productId, displayLanguageId)
                .map(t -> ProductResponse.builder()
                        .productId(response.getProductId())
                        .subcategoryId(response.getSubcategoryId())
                        .subcategoryName(response.getSubcategoryName())
                        .genreId(response.getGenreId())
                        .genreName(response.getGenreName())
                        .languageId(response.getLanguageId())
                        .languageName(response.getLanguageName())
                        .mediaType(response.getMediaType())
                        .episodeCount(response.getEpisodeCount())
                        .title(t.getTitle())
                        .shortDescription(StringUtils.hasText(t.getShortDescription())
                                ? t.getShortDescription() : response.getShortDescription())
                        .description(StringUtils.hasText(t.getDescription())
                                ? t.getDescription() : response.getDescription())
                        .price(response.getPrice())
                        .pages(response.getPages())
                        .duration(response.getDuration())
                        .coverImage(response.getCoverImage())
                        .fileType(response.getFileType())
                        .hasContentFile(response.isHasContentFile())
                        .isRentable(response.getIsRentable())
                        .isLibraryEligible(response.getIsLibraryEligible())
                        .rentRate(response.getRentRate())
                        .minRentDays(response.getMinRentDays())
                        .isAvailable(response.getIsAvailable())
                        .beneficiaries(response.getBeneficiaries())
                        .createdAt(response.getCreatedAt())
                        .updatedAt(response.getUpdatedAt())
                        .build())
                .orElse(response);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateRentFields(request);

        ProductSubcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategory_id", request.getSubcategoryId()));
        Genre genre = resolveGenre(request.getGenreId());
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language", "language_id", request.getLanguageId()));

        Product product = Product.builder()
                .subcategory(subcategory)
                .genre(genre)
                .language(language)
                .mediaType(request.getMediaType() != null ? request.getMediaType() : Product.MediaType.BOOK)
                .episodeCount(request.getEpisodeCount())
                .title(request.getTitle())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .price(request.getPrice())
                .pages(request.getPages())
                .duration(request.getDuration())
                .coverImage(request.getCoverImage())
                .filePath(request.getFilePath())
                .fileType(request.getFileType())
                .isRentable(request.getIsRentable())
                .isLibraryEligible(request.getIsLibraryEligible())
                .rentRate(needsRentRate(request) ? request.getRentRate() : null)
                .minRentDays(request.getIsRentable() ? request.getMinRentDays() : null)
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        Product savedProduct = productRepository.save(product);

        if (request.getBeneficiaries() != null) {
            // Preferred path: one or more splits, going through the same
            // validated addSplit()/replaceAssignments() rules everywhere else
            // (active-beneficiary check, per-item percentage default from
            // BeneficiaryType, <=100% total).
            productBeneficiaryService.replaceAssignments(savedProduct.getProductId(), request.getBeneficiaries());
        } else {
            // Deprecated single-beneficiary path, preserved byte-for-byte for
            // backward compatibility: same historical 10% fallback for an
            // untyped beneficiary that this endpoint has always used, rather
            // than the stricter "no type -> error" rule the new path enforces.
            assignLegacyDefaultBeneficiary(savedProduct, request.getBeneficiaryId());
        }

        ProductResponse response = toResponseWithBeneficiaries(savedProduct, null);
        auditService.log("PRODUCT_CREATE", "PRODUCT", response.getProductId(), request.getTitle());
        return response;
    }

    private void assignLegacyDefaultBeneficiary(Product savedProduct, Long requestedBeneficiaryId) {
        Long benId = requestedBeneficiaryId != null ? requestedBeneficiaryId : 1L;
        beneficiaryRepository.findById(benId).ifPresent(ben -> {
            if (!Boolean.TRUE.equals(ben.getIsActive())) {
                throw new IllegalArgumentException(
                        "Beneficiary '" + ben.getName() + "' is inactive and cannot be assigned to a product");
            }
            java.math.BigDecimal percent = ben.getBeneficiaryType() != null
                    ? ben.getBeneficiaryType().getDefaultRoyaltyPercentage()
                    : new java.math.BigDecimal("10.00");
            productBeneficiaryRepository.save(ProductBeneficiary.builder()
                    .product(savedProduct)
                    .beneficiary(ben)
                    .royaltyPercentage(percent)
                    .build());
        });
    }

    @Override
    @Transactional
    public ProductResponse update(Long productId, ProductRequest request) {
        validateRentFields(request);

        Product product = findEntityOrThrow(productId);

        if (!product.getSubcategory().getSubcategoryId().equals(request.getSubcategoryId())) {
            ProductSubcategory subcategory = subcategoryRepository.findById(request.getSubcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategory", "subcategory_id", request.getSubcategoryId()));
            product.setSubcategory(subcategory);
        }
        product.setGenre(resolveGenre(request.getGenreId()));
        if (!product.getLanguage().getLanguageId().equals(request.getLanguageId())) {
            Language language = languageRepository.findById(request.getLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Language", "language_id", request.getLanguageId()));
            product.setLanguage(language);
        }

        if (request.getMediaType() != null) product.setMediaType(request.getMediaType());
        product.setEpisodeCount(request.getEpisodeCount());
        product.setTitle(request.getTitle());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setPages(request.getPages());
        product.setDuration(request.getDuration());
        // coverImage round-trips fine (it IS exposed on ProductResponse, so an edit
        // form can always pre-fill it correctly), but filePath is deliberately
        // excluded from ProductResponse (see ProductMapper - raw content location
        // must never leak on a public catalog response). That means any caller
        // building an edit form from GET /products/{id} can never see the current
        // filePath, so an unconditional overwrite here would silently null out a
        // previously-attached content file on every unrelated edit (e.g. fixing a
        // price typo). Blank/missing filePath in the request is therefore treated
        // as "leave it alone," not "clear it" - only a non-blank value replaces it.
        if (StringUtils.hasText(request.getCoverImage())) product.setCoverImage(request.getCoverImage());
        if (StringUtils.hasText(request.getFilePath())) product.setFilePath(request.getFilePath());
        product.setFileType(request.getFileType());
        product.setIsRentable(request.getIsRentable());
        product.setIsLibraryEligible(request.getIsLibraryEligible());
        product.setRentRate(needsRentRate(request) ? request.getRentRate() : null);
        product.setMinRentDays(request.getIsRentable() ? request.getMinRentDays() : null);
        if (request.getIsAvailable() != null) product.setIsAvailable(request.getIsAvailable());

        Product savedProduct = productRepository.save(product);

        // null beneficiaries = "don't touch the allocation" (backward compatible -
        // existing PUT callers that don't know about this field can't accidentally
        // wipe a product's royalty splits). An explicit empty list IS a deliberate
        // "remove all beneficiaries" instruction and is honored as such.
        if (request.getBeneficiaries() != null) {
            productBeneficiaryService.replaceAssignments(productId, request.getBeneficiaries());
        }

        ProductResponse response = toResponseWithBeneficiaries(savedProduct, null);
        auditService.log("PRODUCT_UPDATE", "PRODUCT", productId, request.getTitle());
        return response;
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        Product product = findEntityOrThrow(productId);
        // Soft delete only. Products are referenced by CART_ITEMS, PURCHASE_ITEMS, RENT_TRANSACTIONS,
        // USER_LIBRARY etc. - a hard delete would either violate FK constraints or destroy purchase
        // history for users who already own it. is_available=false just pulls it off the storefront.
        product.setIsAvailable(false);
        productRepository.save(product);
        auditService.log("PRODUCT_DELETE", "PRODUCT", productId, product.getTitle());
    }

    /**
     * is_rentable=true requires rent_rate and min_rent_days (min_rent_days only
     * makes sense for a direct rental checkout, not a library borrow, which is
     * instead bounded by the subscription's own end date - see
     * UserLibraryServiceImpl.borrow()).
     *
     * is_library_eligible=true ALSO requires rent_rate, even when the product
     * isn't independently rentable: RoyaltyServiceImpl values a library borrow
     * identically to a rental (rentRate x borrowDays - see
     * UserLibraryServiceImpl.borrow()'s comment), and a null rentRate silently
     * produced a $0.00 gross amount for every borrow of that product, so
     * royalty rows were technically written but always carried a zero payout -
     * indistinguishable from "royalty isn't being recorded at all" from an
     * admin/beneficiary's point of view. Requiring the rate up front catches a
     * misconfigured catalog entry at save time instead of silently under-paying
     * every beneficiary on every borrow of it.
     */
    private void validateRentFields(ProductRequest request) {
        if (Boolean.TRUE.equals(request.getIsRentable())) {
            if (request.getRentRate() == null || request.getMinRentDays() == null) {
                throw new IllegalArgumentException(
                        "rentRate and minRentDays are required when isRentable is true");
            }
        }
        if (Boolean.TRUE.equals(request.getIsLibraryEligible()) && request.getRentRate() == null) {
            throw new IllegalArgumentException(
                    "rentRate is required when isLibraryEligible is true (used to value royalty on borrow)");
        }
    }

    /** rentRate must be persisted whenever it will be read later - by RentServiceImpl for rentals, or by UserLibraryServiceImpl for borrows. */
    private boolean needsRentRate(ProductRequest request) {
        return Boolean.TRUE.equals(request.getIsRentable()) || Boolean.TRUE.equals(request.getIsLibraryEligible());
    }

    private Genre resolveGenre(Long genreId) {
        if (genreId == null) return null;
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "genre_id", genreId));
    }

    private Product findEntityOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));
    }
}
