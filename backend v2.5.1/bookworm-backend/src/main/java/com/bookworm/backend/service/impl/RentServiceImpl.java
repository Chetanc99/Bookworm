package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.RentTransactionResponse;
import com.bookworm.backend.entity.*;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.RentMapper;
import com.bookworm.backend.repository.*;
import com.bookworm.backend.service.RentService;
import com.bookworm.backend.service.RoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors PurchaseServiceImpl's checkout shape, but each product becomes its
 * own RentTransaction (own validity window) rather than line items under one
 * parent. No loyalty points on rent - the scope note only calls out
 * "loyalty earn" for the purchase flow, so this intentionally does not
 * write to LoyaltyLedger.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentServiceImpl implements RentService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RentTransactionRepository rentTransactionRepository;
    private final UserShelfRepository userShelfRepository;
    private final RoyaltyService royaltyService;
    private final RentMapper mapper;

    @Override
    @Transactional
    public List<RentTransactionResponse> checkout(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty - nothing to check out"));

        List<CartItem> rentItems = cartItemRepository.findByCart_CartId(cart.getCartId()).stream()
                .filter(item -> item.getIntent() == CartItem.Intent.RENT)
                .toList();

        if (rentItems.isEmpty()) {
            throw new IllegalArgumentException("No RENT-intent items in cart to check out");
        }

        List<RentTransactionResponse> responses = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (CartItem item : rentItems) {
            Product product = item.getProduct();

            // Re-validate at checkout time - eligibility could have changed since add-to-cart.
            if (!Boolean.TRUE.equals(product.getIsAvailable()) || !Boolean.TRUE.equals(product.getIsRentable())) {
                throw new IllegalArgumentException(
                        "Product '" + product.getTitle() + "' is no longer available for rent");
            }
            Integer rentDays = item.getRentDays();
            if (rentDays == null || (product.getMinRentDays() != null && rentDays < product.getMinRentDays())) {
                throw new IllegalArgumentException(
                        "Invalid rentDays for product '" + product.getTitle() + "'");
            }

            BigDecimal rentRate = product.getRentRate() != null ? product.getRentRate() : BigDecimal.ZERO;
            BigDecimal totalAmount = rentRate.multiply(BigDecimal.valueOf(rentDays));
            LocalDateTime endDate = now.plusDays(rentDays);

            RentTransaction transaction = rentTransactionRepository.save(
                    RentTransaction.builder()
                            .userId(userId)
                            .product(product)
                            .rentDays(rentDays)
                            .rentRate(rentRate)
                            .totalAmount(totalAmount)
                            .startDate(now)
                            .endDate(endDate)
                            .status(RentTransaction.Status.ACTIVE)
                            .build());

            royaltyService.recordForProduct(product, totalAmount,
                    com.bookworm.backend.entity.RoyaltyLedger.SourceType.RENT,
                    transaction.getRentTransactionId());

            userShelfRepository.save(UserShelf.builder()
                    .userId(userId)
                    .product(product)
                    .source(UserShelf.Source.RENT)
                    .sourceReferenceId(transaction.getRentTransactionId())
                    .expiresAt(endDate)
                    .build());

            responses.add(mapper.toResponse(transaction));
        }

        rentItems.forEach(cartItemRepository::delete);

        return responses;
    }

    @Override
    public Page<RentTransactionResponse> getHistory(Long userId, Pageable pageable) {
        return rentTransactionRepository.findByUserId(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public RentTransactionResponse getById(Long userId, Long rentTransactionId) {
        RentTransaction transaction = rentTransactionRepository.findById(rentTransactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RentTransaction", "rent_transaction_id", rentTransactionId));
        if (!transaction.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("RentTransaction", "rent_transaction_id", rentTransactionId);
        }
        return mapper.toResponse(transaction);
    }
}
