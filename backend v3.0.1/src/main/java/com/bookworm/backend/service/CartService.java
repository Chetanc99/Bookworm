package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.CartItemRequest;
import com.bookworm.backend.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, CartItemRequest request);

    CartResponse updateItem(Long userId, Long cartItemId, CartItemRequest request);

    CartResponse removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}
