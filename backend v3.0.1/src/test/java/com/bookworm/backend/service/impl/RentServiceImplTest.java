package com.bookworm.backend.service.impl;

import com.bookworm.backend.entity.*;
import com.bookworm.backend.mapper.RentMapper;
import com.bookworm.backend.repository.CartItemRepository;
import com.bookworm.backend.repository.CartRepository;
import com.bookworm.backend.repository.RentTransactionRepository;
import com.bookworm.backend.repository.UserShelfRepository;
import com.bookworm.backend.service.RoyaltyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Regression check that the existing RENT royalty integration keeps working
 * as-is while the library-borrow gap is fixed alongside it.
 */
@ExtendWith(MockitoExtension.class)
class RentServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private RentTransactionRepository rentTransactionRepository;
    @Mock private UserShelfRepository userShelfRepository;
    @Mock private RoyaltyService royaltyService;
    @Mock private com.bookworm.backend.service.AcquisitionEligibilityService acquisitionEligibilityService;
    @Mock private RentMapper mapper;

    private RentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RentServiceImpl(cartRepository, cartItemRepository, rentTransactionRepository,
                userShelfRepository, royaltyService, acquisitionEligibilityService, mapper);
    }

    @Test
    void checkout_recordsRoyaltyPerRentedProductUsingRentRateTimesRentDays() {
        Product product = Product.builder()
                .productId(5L).title("Dune").isAvailable(true).isRentable(true)
                .rentRate(new BigDecimal("3.00")).minRentDays(2).build();

        Cart cart = Cart.builder().cartId(1L).userId(1L).build();
        CartItem item = CartItem.builder()
                .cartItemId(1L).cart(cart).product(product).intent(CartItem.Intent.RENT).rentDays(4).build();

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart_CartId(1L)).thenReturn(List.of(item));

        RentTransaction saved = RentTransaction.builder()
                .rentTransactionId(77L).userId(1L).product(product).rentDays(4)
                .rentRate(new BigDecimal("3.00")).totalAmount(new BigDecimal("12.00"))
                .status(RentTransaction.Status.ACTIVE).build();
        when(rentTransactionRepository.save(any(RentTransaction.class))).thenReturn(saved);

        service.checkout(1L);

        verify(royaltyService).recordForProduct(
                eq(product), eq(new BigDecimal("12.00")), eq(RoyaltyLedger.SourceType.RENT), eq(77L));
    }
}
