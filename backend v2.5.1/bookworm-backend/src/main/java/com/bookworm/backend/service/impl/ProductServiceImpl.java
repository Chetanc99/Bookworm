package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.ProductResponse;
import com.bookworm.backend.entity.Genre;
import com.bookworm.backend.entity.Language;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductSubcategory;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.ProductMapper;
import com.bookworm.backend.entity.ProductBeneficiary;
import com.bookworm.backend.repository.BeneficiaryRepository;
import com.bookworm.backend.repository.GenreRepository;
import com.bookworm.backend.repository.LanguageRepository;
import com.bookworm.backend.repository.ProductBeneficiaryRepository;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.repository.ProductSubcategoryRepository;
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
    private final AuditService auditService;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> browse(Long subcategoryId, Long genreId, Long languageId,
                                         Boolean isRentable, String keyword, Pageable pageable) {
        String cleanKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Product> page = productRepository.filterProducts(subcategoryId, genreId, languageId, isRentable, cleanKeyword, pageable);
        return page.map(this::toResponseWithBeneficiaries);
    }

    @Override
    public ProductResponse getById(Long productId) {
        return toResponseWithBeneficiaries(findEntityOrThrow(productId));
    }

    private ProductResponse toResponseWithBeneficiaries(Product product) {
        return productMapper.toResponse(product, productBeneficiaryService.getByProduct(product.getProductId()));
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
                .rentRate(request.getIsRentable() ? request.getRentRate() : null)
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

        ProductResponse response = toResponseWithBeneficiaries(savedProduct);
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
        product.setRentRate(request.getIsRentable() ? request.getRentRate() : null);
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

        ProductResponse response = toResponseWithBeneficiaries(savedProduct);
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

    /** is_rentable=true requires rent_rate and min_rent_days; is_rentable=false must not carry stale rent data. */
    private void validateRentFields(ProductRequest request) {
        if (Boolean.TRUE.equals(request.getIsRentable())) {
            if (request.getRentRate() == null || request.getMinRentDays() == null) {
                throw new IllegalArgumentException(
                        "rentRate and minRentDays are required when isRentable is true");
            }
        }
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
