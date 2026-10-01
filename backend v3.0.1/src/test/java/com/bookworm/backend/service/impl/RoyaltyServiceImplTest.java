package com.bookworm.backend.service.impl;

import com.bookworm.backend.entity.Beneficiary;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.ProductBeneficiary;
import com.bookworm.backend.entity.RoyaltyLedger;
import com.bookworm.backend.mapper.RoyaltyMapper;
import com.bookworm.backend.repository.BeneficiaryRepository;
import com.bookworm.backend.repository.ProductBeneficiaryRepository;
import com.bookworm.backend.repository.RoyaltyLedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers RoyaltyServiceImpl.recordForProduct(), the shared entry point every
 * revenue/usage event (purchase, rent, and now library borrow) funnels
 * through - so correctness here backstops all of them.
 */
@ExtendWith(MockitoExtension.class)
class RoyaltyServiceImplTest {

    @Mock private ProductBeneficiaryRepository productBeneficiaryRepository;
    @Mock private BeneficiaryRepository beneficiaryRepository;
    @Mock private RoyaltyLedgerRepository royaltyLedgerRepository;
    @Mock private RoyaltyMapper mapper;

    private RoyaltyServiceImpl service;

    private Product product;

    @BeforeEach
    void setUp() {
        // Real (not mocked) calculator - it's a pure component, so exercising the
        // actual rounding-safe split logic here is more meaningful than stubbing it.
        service = new RoyaltyServiceImpl(productBeneficiaryRepository, beneficiaryRepository,
                royaltyLedgerRepository, new RoyaltySplitCalculator(), mapper);
        product = Product.builder().productId(5L).title("Dune").build();
    }

    @Test
    void recordForProduct_splitsGrossAmountAcrossConfiguredBeneficiaries() {
        Beneficiary author = Beneficiary.builder().beneficiaryId(1L).name("Author").isActive(true).build();
        Beneficiary publisher = Beneficiary.builder().beneficiaryId(2L).name("Publisher").isActive(true).build();

        ProductBeneficiary split1 = ProductBeneficiary.builder()
                .product(product).beneficiary(author).royaltyPercentage(new BigDecimal("70.00")).build();
        ProductBeneficiary split2 = ProductBeneficiary.builder()
                .product(product).beneficiary(publisher).royaltyPercentage(new BigDecimal("30.00")).build();

        when(productBeneficiaryRepository.findByProduct_ProductId(5L)).thenReturn(List.of(split1, split2));

        service.recordForProduct(product, new BigDecimal("100.00"), RoyaltyLedger.SourceType.PURCHASE, 42L);

        ArgumentCaptor<RoyaltyLedger> captor = ArgumentCaptor.forClass(RoyaltyLedger.class);
        verify(royaltyLedgerRepository, times(2)).save(captor.capture());

        List<RoyaltyLedger> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);

        RoyaltyLedger authorLedger = saved.stream().filter(l -> l.getBeneficiary() == author).findFirst().orElseThrow();
        assertThat(authorLedger.getRoyaltyAmount()).isEqualByComparingTo("70.00");
        assertThat(authorLedger.getSourceType()).isEqualTo(RoyaltyLedger.SourceType.PURCHASE);
        assertThat(authorLedger.getSourceReferenceId()).isEqualTo(42L);

        RoyaltyLedger publisherLedger = saved.stream().filter(l -> l.getBeneficiary() == publisher).findFirst().orElseThrow();
        assertThat(publisherLedger.getRoyaltyAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void recordForProduct_zeroGrossAmount_stillWritesLedgerRowsWithZeroRoyalty() {
        // Mirrors a LIBRARY borrow of a product with no rentRate configured -
        // the event still happened and should still be traceable, just worth 0.
        Beneficiary author = Beneficiary.builder().beneficiaryId(1L).name("Author").isActive(true).build();
        ProductBeneficiary split = ProductBeneficiary.builder()
                .product(product).beneficiary(author).royaltyPercentage(new BigDecimal("100.00")).build();
        when(productBeneficiaryRepository.findByProduct_ProductId(5L)).thenReturn(List.of(split));

        service.recordForProduct(product, BigDecimal.ZERO, RoyaltyLedger.SourceType.LIBRARY, 7L);

        ArgumentCaptor<RoyaltyLedger> captor = ArgumentCaptor.forClass(RoyaltyLedger.class);
        verify(royaltyLedgerRepository).save(captor.capture());
        assertThat(captor.getValue().getRoyaltyAmount()).isEqualByComparingTo("0.00");
        assertThat(captor.getValue().getSourceType()).isEqualTo(RoyaltyLedger.SourceType.LIBRARY);
    }

    @Test
    void recordForProduct_noBeneficiariesConfiguredAndNoneExistAnywhere_writesNothing() {
        when(productBeneficiaryRepository.findByProduct_ProductId(5L)).thenReturn(List.of());
        when(beneficiaryRepository.findAll()).thenReturn(List.of());

        service.recordForProduct(product, new BigDecimal("50.00"), RoyaltyLedger.SourceType.RENT, 1L);

        verify(royaltyLedgerRepository, never()).save(any());
    }

    @Test
    void recordForProduct_fallsBackToDefaultBeneficiaryWhenProductHasNoSplitsConfigured() {
        Beneficiary fallback = Beneficiary.builder().beneficiaryId(9L).name("Default").isActive(true).build();
        when(productBeneficiaryRepository.findByProduct_ProductId(5L)).thenReturn(List.of());
        when(beneficiaryRepository.findAll()).thenReturn(List.of(fallback));

        service.recordForProduct(product, new BigDecimal("100.00"), RoyaltyLedger.SourceType.RENT, 1L);

        ArgumentCaptor<RoyaltyLedger> captor = ArgumentCaptor.forClass(RoyaltyLedger.class);
        verify(royaltyLedgerRepository).save(captor.capture());
        assertThat(captor.getValue().getBeneficiary()).isEqualTo(fallback);
        assertThat(captor.getValue().getRoyaltyPercentage()).isEqualByComparingTo("10.00");
        assertThat(captor.getValue().getRoyaltyAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void recordForProduct_unevenPercentages_beneficiaryAmountsSumExactlyToTotal() {
        // 33.34 / 33.33 / 33.33 of 10.00: independently HALF_UP-rounding each share
        // (the old behavior) gives 3.33 + 3.33 + 3.33 = 9.99, a cent short of the
        // true 10.00 total. The largest-remainder split must recover that cent.
        Beneficiary b1 = Beneficiary.builder().beneficiaryId(1L).name("B1").isActive(true).build();
        Beneficiary b2 = Beneficiary.builder().beneficiaryId(2L).name("B2").isActive(true).build();
        Beneficiary b3 = Beneficiary.builder().beneficiaryId(3L).name("B3").isActive(true).build();

        ProductBeneficiary s1 = ProductBeneficiary.builder().product(product).beneficiary(b1).royaltyPercentage(new BigDecimal("33.34")).build();
        ProductBeneficiary s2 = ProductBeneficiary.builder().product(product).beneficiary(b2).royaltyPercentage(new BigDecimal("33.33")).build();
        ProductBeneficiary s3 = ProductBeneficiary.builder().product(product).beneficiary(b3).royaltyPercentage(new BigDecimal("33.33")).build();
        when(productBeneficiaryRepository.findByProduct_ProductId(5L)).thenReturn(List.of(s1, s2, s3));

        service.recordForProduct(product, new BigDecimal("10.00"), RoyaltyLedger.SourceType.PURCHASE, 1L);

        ArgumentCaptor<RoyaltyLedger> captor = ArgumentCaptor.forClass(RoyaltyLedger.class);
        verify(royaltyLedgerRepository, times(3)).save(captor.capture());

        BigDecimal sum = captor.getAllValues().stream()
                .map(RoyaltyLedger::getRoyaltyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("10.00");
    }
}
