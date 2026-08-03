package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.BorrowRequest;
import com.bookworm.backend.entity.*;
import com.bookworm.backend.mapper.UserLibraryMapper;
import com.bookworm.backend.repository.*;
import com.bookworm.backend.service.RoyaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Covers the borrow() royalty gap: UserLibraryServiceImpl previously never
 * called RoyaltyService.recordForProduct(), so library-lend revenue never
 * reached the royalty ledger. These tests pin the fix in place and document
 * the (rentRate x borrowDays) valuation, mirroring RentServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class UserLibraryServiceImplTest {

    @Mock private LibraryPackageRepository libraryPackageRepository;
    @Mock private UserLibraryPackageRepository userLibraryPackageRepository;
    @Mock private UserLibraryRepository userLibraryRepository;
    @Mock private UserShelfRepository userShelfRepository;
    @Mock private ProductRepository productRepository;
    @Mock private PurchaseTransactionRepository purchaseTransactionRepository;
    @Mock private PurchaseItemRepository purchaseItemRepository;
    @Mock private RoyaltyService royaltyService;
    @Mock private UserLibraryMapper mapper;

    private UserLibraryServiceImpl service;

    private UserLibraryPackage activeSubscription;
    private LibraryPackage pkg;
    private Product product;

    @BeforeEach
    void setUp() {
        service = new UserLibraryServiceImpl(
                libraryPackageRepository, userLibraryPackageRepository, userLibraryRepository,
                userShelfRepository, productRepository, purchaseTransactionRepository,
                purchaseItemRepository, royaltyService, mapper);

        pkg = LibraryPackage.builder()
                .libraryPackageId(1L)
                .packageName("Basic")
                .price(new BigDecimal("100.00"))
                .durationDays(30)
                .maxConcurrentBorrows(3)
                .isActive(true)
                .build();

        activeSubscription = UserLibraryPackage.builder()
                .userLibraryPackageId(10L)
                .userId(1L)
                .libraryPackage(pkg)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(29))
                .status(UserLibraryPackage.Status.ACTIVE)
                .build();

        product = Product.builder()
                .productId(5L)
                .title("Dune")
                .price(new BigDecimal("20.00"))
                .isAvailable(true)
                .isLibraryEligible(true)
                .rentRate(new BigDecimal("2.00"))
                .build();
    }

    @Test
    void borrow_recordsRoyaltyForProductUsingRentRateTimesBorrowDays() {
        when(userLibraryPackageRepository.findByUserIdAndStatus(1L, UserLibraryPackage.Status.ACTIVE))
                .thenReturn(Optional.of(activeSubscription));
        when(userLibraryRepository.countByUserLibraryPackage_UserLibraryPackageIdAndStatus(10L, UserLibrary.Status.BORROWED))
                .thenReturn(0L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        UserLibrary saved = UserLibrary.builder()
                .userLibraryId(99L)
                .userLibraryPackage(activeSubscription)
                .product(product)
                .dueDate(LocalDateTime.now().plusDays(5))
                .status(UserLibrary.Status.BORROWED)
                .build();
        when(userLibraryRepository.save(any(UserLibrary.class))).thenReturn(saved);

        BorrowRequest request = new BorrowRequest();
        request.setProductId(5L);
        request.setBorrowDays(5);

        service.borrow(1L, request);

        ArgumentCaptor<BigDecimal> grossCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(royaltyService).recordForProduct(
                eq(product), grossCaptor.capture(), eq(RoyaltyLedger.SourceType.LIBRARY), eq(99L));

        // rentRate (2.00) x borrowDays (5) = 10.00
        assertThat(grossCaptor.getValue()).isEqualByComparingTo("10.00");
    }

    @Test
    void borrow_withNullRentRate_recordsZeroGrossAmountRatherThanSkippingRoyalty() {
        product.setRentRate(null);

        when(userLibraryPackageRepository.findByUserIdAndStatus(1L, UserLibraryPackage.Status.ACTIVE))
                .thenReturn(Optional.of(activeSubscription));
        when(userLibraryRepository.countByUserLibraryPackage_UserLibraryPackageIdAndStatus(10L, UserLibrary.Status.BORROWED))
                .thenReturn(0L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        UserLibrary saved = UserLibrary.builder()
                .userLibraryId(100L)
                .userLibraryPackage(activeSubscription)
                .product(product)
                .dueDate(LocalDateTime.now().plusDays(3))
                .status(UserLibrary.Status.BORROWED)
                .build();
        when(userLibraryRepository.save(any(UserLibrary.class))).thenReturn(saved);

        BorrowRequest request = new BorrowRequest();
        request.setProductId(5L);
        request.setBorrowDays(3);

        service.borrow(1L, request);

        ArgumentCaptor<BigDecimal> grossCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(royaltyService).recordForProduct(
                eq(product), grossCaptor.capture(), eq(RoyaltyLedger.SourceType.LIBRARY), eq(100L));

        assertThat(grossCaptor.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void borrow_stillGrantsShelfAccessAfterRoyaltyRecording() {
        when(userLibraryPackageRepository.findByUserIdAndStatus(1L, UserLibraryPackage.Status.ACTIVE))
                .thenReturn(Optional.of(activeSubscription));
        when(userLibraryRepository.countByUserLibraryPackage_UserLibraryPackageIdAndStatus(10L, UserLibrary.Status.BORROWED))
                .thenReturn(0L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        UserLibrary saved = UserLibrary.builder()
                .userLibraryId(101L)
                .userLibraryPackage(activeSubscription)
                .product(product)
                .dueDate(LocalDateTime.now().plusDays(2))
                .status(UserLibrary.Status.BORROWED)
                .build();
        when(userLibraryRepository.save(any(UserLibrary.class))).thenReturn(saved);

        BorrowRequest request = new BorrowRequest();
        request.setProductId(5L);
        request.setBorrowDays(2);

        service.borrow(1L, request);

        verify(userShelfRepository).save(argThat(shelf ->
                shelf.getSource() == UserShelf.Source.LIBRARY
                        && shelf.getSourceReferenceId().equals(101L)
                        && shelf.getUserId().equals(1L)));
    }
}
