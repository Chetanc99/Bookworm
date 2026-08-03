package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductBeneficiaryRequest;
import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;
import com.bookworm.backend.entity.*;
import com.bookworm.backend.mapper.ProductMapper;
import com.bookworm.backend.repository.*;
import com.bookworm.backend.service.AuditService;
import com.bookworm.backend.service.ProductBeneficiaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Covers the create()/update() <-> beneficiary-allocation wiring: the
 * `beneficiaries` list (preferred) versus the deprecated single
 * `beneficiaryId` fallback, and that update() leaves the allocation alone
 * when the caller doesn't send `beneficiaries` at all (backward compat).
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductSubcategoryRepository subcategoryRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private LanguageRepository languageRepository;
    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private ProductBeneficiaryRepository productBeneficiaryRepository;
    @Mock private ProductBeneficiaryService productBeneficiaryService;
    @Mock private AuditService auditService;
    @Mock private ProductMapper productMapper;

    private ProductServiceImpl service;

    private ProductSubcategory subcategory;
    private Language language;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(productRepository, subcategoryRepository, genreRepository,
                languageRepository, beneficiaryRepository, productBeneficiaryRepository,
                productBeneficiaryService, auditService, productMapper);

        subcategory = ProductSubcategory.builder().subcategoryId(1L).build();
        language = Language.builder().languageId(1L).build();

        lenient().when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));
        lenient().when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
        lenient().when(productBeneficiaryService.getByProduct(any())).thenReturn(List.of());
    }

    private ProductRequest baseRequest() {
        ProductRequest request = new ProductRequest();
        request.setSubcategoryId(1L);
        request.setLanguageId(1L);
        request.setTitle("Dune");
        request.setPrice(new BigDecimal("20.00"));
        request.setIsRentable(false);
        request.setIsLibraryEligible(false);
        return request;
    }

    @Test
    void create_withBeneficiariesList_delegatesToReplaceAssignments() {
        Product saved = Product.builder().productId(5L).subcategory(subcategory).language(language)
                .title("Dune").isRentable(false).isLibraryEligible(false).build();
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponse(any(Product.class), any())).thenReturn(
                com.bookworm.backend.dto.response.ProductResponse.builder().productId(5L).build());

        ProductBeneficiaryRequest split = new ProductBeneficiaryRequest();
        split.setBeneficiaryId(2L);
        split.setRoyaltyPercentage(new BigDecimal("40.00"));

        ProductRequest request = baseRequest();
        request.setBeneficiaries(List.of(split));

        service.create(request);

        verify(productBeneficiaryService).replaceAssignments(5L, List.of(split));
        verify(beneficiaryRepository, never()).findById(any());
    }

    @Test
    void create_withoutBeneficiariesList_usesLegacySingleBeneficiaryFallback() {
        Product saved = Product.builder().productId(5L).subcategory(subcategory).language(language)
                .title("Dune").isRentable(false).isLibraryEligible(false).build();
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponse(any(Product.class), any())).thenReturn(
                com.bookworm.backend.dto.response.ProductResponse.builder().productId(5L).build());

        Beneficiary legacyDefault = Beneficiary.builder().beneficiaryId(1L).name("Default").isActive(true).build();
        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(legacyDefault));

        ProductRequest request = baseRequest(); // no beneficiaries, no beneficiaryId -> falls back to id 1

        service.create(request);

        ArgumentCaptor<ProductBeneficiary> captor = ArgumentCaptor.forClass(ProductBeneficiary.class);
        verify(productBeneficiaryRepository).save(captor.capture());
        // No BeneficiaryType on this beneficiary -> historical 10% fallback.
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getRoyaltyPercentage())
                .isEqualByComparingTo("10.00");
        verify(productBeneficiaryService, never()).replaceAssignments(any(), any());
    }

    @Test
    void create_legacyFallback_rejectsInactiveBeneficiary() {
        Product saved = Product.builder().productId(5L).subcategory(subcategory).language(language)
                .title("Dune").isRentable(false).isLibraryEligible(false).build();
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Beneficiary inactive = Beneficiary.builder().beneficiaryId(1L).name("Retired").isActive(false).build();
        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(inactive));

        ProductRequest request = baseRequest();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");

        verify(productBeneficiaryRepository, never()).save(any());
    }

    @Test
    void update_withNullBeneficiariesList_leavesAllocationUntouched() {
        Product existing = Product.builder().productId(5L).subcategory(subcategory).language(language)
                .title("Old Title").isRentable(false).isLibraryEligible(false).build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);
        when(productMapper.toResponse(any(Product.class), any())).thenReturn(
                com.bookworm.backend.dto.response.ProductResponse.builder().productId(5L).build());

        ProductRequest request = baseRequest(); // beneficiaries left null

        service.update(5L, request);

        verify(productBeneficiaryService, never()).replaceAssignments(any(), any());
    }

    @Test
    void update_withBeneficiariesList_replacesAllocation() {
        Product existing = Product.builder().productId(5L).subcategory(subcategory).language(language)
                .title("Old Title").isRentable(false).isLibraryEligible(false).build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);
        when(productMapper.toResponse(any(Product.class), any())).thenReturn(
                com.bookworm.backend.dto.response.ProductResponse.builder().productId(5L).build());
        when(productBeneficiaryService.replaceAssignments(eq(5L), any()))
                .thenReturn(List.<ProductBeneficiaryResponse>of());

        ProductBeneficiaryRequest split = new ProductBeneficiaryRequest();
        split.setBeneficiaryId(3L);
        split.setRoyaltyPercentage(new BigDecimal("50.00"));

        ProductRequest request = baseRequest();
        request.setBeneficiaries(List.of(split));

        service.update(5L, request);

        verify(productBeneficiaryService).replaceAssignments(5L, List.of(split));
    }
}
