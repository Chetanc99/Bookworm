package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.ProductBeneficiaryRequest;
import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;
import com.bookworm.backend.entity.Beneficiary;
import com.bookworm.backend.entity.BeneficiaryType;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductBeneficiary;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.mapper.ProductBeneficiaryMapper;
import com.bookworm.backend.repository.BeneficiaryRepository;
import com.bookworm.backend.repository.ProductBeneficiaryRepository;
import com.bookworm.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductBeneficiaryServiceImplTest {

    @Mock private ProductBeneficiaryRepository productBeneficiaryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private ProductBeneficiaryMapper mapper;

    private ProductBeneficiaryServiceImpl service;

    private Product product;

    @BeforeEach
    void setUp() {
        service = new ProductBeneficiaryServiceImpl(
                productBeneficiaryRepository, productRepository, beneficiaryRepository, mapper);
        product = Product.builder().productId(1L).title("Dune").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    }

    @Test
    void addSplit_withExplicitPercentage_ignoresBeneficiaryTypeDefault() {
        BeneficiaryType type = BeneficiaryType.builder().beneficiaryTypeId(1L).name("Author")
                .defaultRoyaltyPercentage(new BigDecimal("15.00")).isActive(true).build();
        Beneficiary beneficiary = Beneficiary.builder().beneficiaryId(2L).name("Frank")
                .beneficiaryType(type).isActive(true).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(productBeneficiaryRepository.existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(1L, 2L)).thenReturn(false);
        when(productBeneficiaryRepository.sumRoyaltyPercentageByProduct(1L)).thenReturn(BigDecimal.ZERO);

        ProductBeneficiaryRequest request = new ProductBeneficiaryRequest();
        request.setBeneficiaryId(2L);
        request.setRoyaltyPercentage(new BigDecimal("25.00"));

        service.addSplit(1L, request);

        ArgumentCaptor<ProductBeneficiary> captor = ArgumentCaptor.forClass(ProductBeneficiary.class);
        verify(productBeneficiaryRepository).save(captor.capture());
        assertThat(captor.getValue().getRoyaltyPercentage()).isEqualByComparingTo("25.00");
    }

    @Test
    void addSplit_withNoPercentage_defaultsFromBeneficiaryType() {
        BeneficiaryType type = BeneficiaryType.builder().beneficiaryTypeId(1L).name("Narrator")
                .defaultRoyaltyPercentage(new BigDecimal("12.50")).isActive(true).build();
        Beneficiary beneficiary = Beneficiary.builder().beneficiaryId(2L).name("Jane")
                .beneficiaryType(type).isActive(true).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(productBeneficiaryRepository.existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(1L, 2L)).thenReturn(false);
        when(productBeneficiaryRepository.sumRoyaltyPercentageByProduct(1L)).thenReturn(BigDecimal.ZERO);

        ProductBeneficiaryRequest request = new ProductBeneficiaryRequest();
        request.setBeneficiaryId(2L);
        request.setRoyaltyPercentage(null);

        service.addSplit(1L, request);

        ArgumentCaptor<ProductBeneficiary> captor = ArgumentCaptor.forClass(ProductBeneficiary.class);
        verify(productBeneficiaryRepository).save(captor.capture());
        assertThat(captor.getValue().getRoyaltyPercentage()).isEqualByComparingTo("12.50");
    }

    @Test
    void addSplit_withNoPercentageAndNoBeneficiaryType_throws() {
        Beneficiary beneficiary = Beneficiary.builder().beneficiaryId(2L).name("NoType").isActive(true).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(productBeneficiaryRepository.existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(1L, 2L)).thenReturn(false);

        ProductBeneficiaryRequest request = new ProductBeneficiaryRequest();
        request.setBeneficiaryId(2L);
        request.setRoyaltyPercentage(null);

        assertThatThrownBy(() -> service.addSplit(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no BeneficiaryType");

        verify(productBeneficiaryRepository, never()).save(any());
    }

    @Test
    void addSplit_exceedingTotalOf100Percent_throws() {
        Beneficiary beneficiary = Beneficiary.builder().beneficiaryId(2L).name("Someone").isActive(true).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(productBeneficiaryRepository.existsByProduct_ProductIdAndBeneficiary_BeneficiaryId(1L, 2L)).thenReturn(false);
        when(productBeneficiaryRepository.sumRoyaltyPercentageByProduct(1L)).thenReturn(new BigDecimal("90.00"));

        ProductBeneficiaryRequest request = new ProductBeneficiaryRequest();
        request.setBeneficiaryId(2L);
        request.setRoyaltyPercentage(new BigDecimal("20.00"));

        assertThatThrownBy(() -> service.addSplit(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds 100%");

        verify(productBeneficiaryRepository, never()).save(any());
    }

    @Test
    void addSplit_inactiveBeneficiary_throws() {
        Beneficiary inactive = Beneficiary.builder().beneficiaryId(2L).name("Retired").isActive(false).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(inactive));

        ProductBeneficiaryRequest request = new ProductBeneficiaryRequest();
        request.setBeneficiaryId(2L);
        request.setRoyaltyPercentage(new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.addSplit(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");

        verify(productBeneficiaryRepository, never()).save(any());
    }

    @Test
    void replaceAssignments_duplicateBeneficiaryWithinRequest_throws() {
        ProductBeneficiaryRequest first = new ProductBeneficiaryRequest();
        first.setBeneficiaryId(2L);
        first.setRoyaltyPercentage(new BigDecimal("10.00"));
        ProductBeneficiaryRequest duplicate = new ProductBeneficiaryRequest();
        duplicate.setBeneficiaryId(2L);
        duplicate.setRoyaltyPercentage(new BigDecimal("20.00"));

        assertThatThrownBy(() -> service.replaceAssignments(1L, List.of(first, duplicate)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(productBeneficiaryRepository, never()).deleteAll(any());
        verify(productBeneficiaryRepository, never()).saveAll(any());
    }

    @Test
    void replaceAssignments_validList_deletesExistingThenSavesNewOnes() {
        Beneficiary b1 = Beneficiary.builder().beneficiaryId(2L).name("Author").isActive(true).build();
        Beneficiary b2 = Beneficiary.builder().beneficiaryId(3L).name("Publisher").isActive(true).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(b1));
        when(beneficiaryRepository.findById(3L)).thenReturn(Optional.of(b2));

        List<ProductBeneficiary> existing = List.of(
                ProductBeneficiary.builder().productBeneficiaryId(99L).product(product).build());
        when(productBeneficiaryRepository.findByProduct_ProductId(1L)).thenReturn(existing);
        when(productBeneficiaryRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(ProductBeneficiary.class))).thenReturn(
                ProductBeneficiaryResponse.builder().build());

        ProductBeneficiaryRequest r1 = new ProductBeneficiaryRequest();
        r1.setBeneficiaryId(2L);
        r1.setRoyaltyPercentage(new BigDecimal("60.00"));
        ProductBeneficiaryRequest r2 = new ProductBeneficiaryRequest();
        r2.setBeneficiaryId(3L);
        r2.setRoyaltyPercentage(new BigDecimal("40.00"));

        List<ProductBeneficiaryResponse> result = service.replaceAssignments(1L, List.of(r1, r2));

        verify(productBeneficiaryRepository).deleteAll(existing);
        ArgumentCaptor<List<ProductBeneficiary>> captor = ArgumentCaptor.forClass(List.class);
        verify(productBeneficiaryRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(result).hasSize(2);
    }

    @Test
    void replaceAssignments_totalOver100Percent_throwsAndDoesNotDelete() {
        Beneficiary b1 = Beneficiary.builder().beneficiaryId(2L).name("Author").isActive(true).build();
        Beneficiary b2 = Beneficiary.builder().beneficiaryId(3L).name("Publisher").isActive(true).build();
        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(b1));
        when(beneficiaryRepository.findById(3L)).thenReturn(Optional.of(b2));

        ProductBeneficiaryRequest r1 = new ProductBeneficiaryRequest();
        r1.setBeneficiaryId(2L);
        r1.setRoyaltyPercentage(new BigDecimal("70.00"));
        ProductBeneficiaryRequest r2 = new ProductBeneficiaryRequest();
        r2.setBeneficiaryId(3L);
        r2.setRoyaltyPercentage(new BigDecimal("40.00"));

        assertThatThrownBy(() -> service.replaceAssignments(1L, List.of(r1, r2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds 100%");

        verify(productBeneficiaryRepository, never()).deleteAll(any());
    }
}
