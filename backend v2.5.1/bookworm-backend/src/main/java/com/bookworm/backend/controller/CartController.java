package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.CartItemRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.CartResponse;
import com.bookworm.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Nested under /users/{userId}/cart rather than reading the user from a
 * security context - there's no auth/JWT layer yet (USERS module isn't
 * built). userId is taken as a path variable for now; swapping this for
 * "current authenticated user" later is a controller-only change, since the
 * service layer already takes userId as a plain parameter.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "One active cart per user - add/update/remove items before checkout")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get (or lazily create) the user's active cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a product to the cart (rejects if unavailable / not rent-eligible)")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @PathVariable Long userId, @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", cartService.addItem(userId, request)));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update a cart item's intent or rentDays")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long userId, @PathVariable Long cartItemId, @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cartService.updateItem(userId, cartItemId, request)));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove a single item from the cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long userId, @PathVariable Long cartItemId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed", cartService.removeItem(userId, cartItemId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from the cart (e.g. after checkout)")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
