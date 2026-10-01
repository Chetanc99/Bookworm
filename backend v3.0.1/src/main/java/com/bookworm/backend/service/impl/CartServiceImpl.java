package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.CartItemRequest;
import com.bookworm.backend.dto.response.CartResponse;
import com.bookworm.backend.entity.Cart;
import com.bookworm.backend.entity.CartItem;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.exception.DuplicateResourceException;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.CartMapper;
import com.bookworm.backend.repository.CartItemRepository;
import com.bookworm.backend.repository.CartRepository;
import com.bookworm.backend.repository.ProductRepository;
import com.bookworm.backend.service.AcquisitionEligibilityService;
import com.bookworm.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Add-to-cart validation is fail-fast: eligibility (is_available,
 * is_rentable, min_rent_days) is checked here, at the API layer, rather than
 * deferred to Purchase/Rent transaction time. A rejected add never reaches
 * the DB, so the cart can never hold an item that couldn't legally be
 * checked out - Purchase/Rent flows can trust cart contents without
 * re-validating eligibility from scratch.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AcquisitionEligibilityService acquisitionEligibilityService;
    private final CartMapper mapper;

    @Override
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        return mapper.toResponse(cart, items);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", request.getProductId()));

        validateEligibility(product, request);
        acquisitionEligibilityService.validate(userId, product.getProductId(), toAcquisitionType(request.getIntent()));

        if (cartItemRepository.existsByCart_CartIdAndProduct_ProductIdAndIntent(
                cart.getCartId(), product.getProductId(), request.getIntent())) {
            throw new DuplicateResourceException(
                    "This product is already in the cart with the same intent");
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .product(product)
                .intent(request.getIntent())
                .rentDays(request.getIntent() == CartItem.Intent.RENT ? request.getRentDays() : null)
                .build();
        cartItemRepository.save(item);

        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        return mapper.toResponse(cart, items);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = requireOwnedItem(cart, cartItemId);
        Product product = item.getProduct();

        // Allow flipping intent (e.g. RENT -> PURCHASE) or changing rentDays; re-validate either way.
        validateEligibility(product, request);
        acquisitionEligibilityService.validate(userId, product.getProductId(), toAcquisitionType(request.getIntent()));

        item.setIntent(request.getIntent());
        item.setRentDays(request.getIntent() == CartItem.Intent.RENT ? request.getRentDays() : null);
        cartItemRepository.save(item);

        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        return mapper.toResponse(cart, items);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = requireOwnedItem(cart, cartItemId);
        cartItemRepository.delete(item);

        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        return mapper.toResponse(cart, items);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
    }

    // --- helpers ---

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
    }

    private CartItem requireOwnedItem(Cart cart, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "cart_item_id", cartItemId));
        if (!item.getCart().getCartId().equals(cart.getCartId())) {
            throw new ResourceNotFoundException("CartItem", "cart_item_id", cartItemId);
        }
        return item;
    }

    // Cart only ever holds PURCHASE/RENT intent (CartItem.Intent has no BORROW
    // value - library borrows never pass through the cart, see
    // UserLibraryServiceImpl.borrow(), which calls AcquisitionEligibilityService
    // directly with AcquisitionType.BORROW instead).
    private AcquisitionEligibilityService.AcquisitionType toAcquisitionType(CartItem.Intent intent) {
        return intent == CartItem.Intent.RENT
                ? AcquisitionEligibilityService.AcquisitionType.RENT
                : AcquisitionEligibilityService.AcquisitionType.PURCHASE;
    }

    private void validateEligibility(Product product, CartItemRequest request) {
        if (!Boolean.TRUE.equals(product.getIsAvailable())) {
            throw new IllegalArgumentException("Product is not available");
        }
        if (request.getIntent() == CartItem.Intent.RENT) {
            if (!Boolean.TRUE.equals(product.getIsRentable())) {
                throw new IllegalArgumentException("Product is not eligible for rent");
            }
            if (request.getRentDays() == null) {
                throw new IllegalArgumentException("rentDays is required when intent is RENT");
            }
            if (product.getMinRentDays() != null && request.getRentDays() < product.getMinRentDays()) {
                throw new IllegalArgumentException(
                        "rentDays must be at least " + product.getMinRentDays() + " for this product");
            }
        }
    }
}
