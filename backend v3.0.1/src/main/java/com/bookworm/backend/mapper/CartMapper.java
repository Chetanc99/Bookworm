package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.CartItemResponse;
import com.bookworm.backend.dto.response.CartResponse;
import com.bookworm.backend.entity.Cart;
import com.bookworm.backend.entity.CartItem;
import com.bookworm.backend.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartItemResponse toItemResponse(CartItem entity) {
        Product product = entity.getProduct();
        return CartItemResponse.builder()
                .cartItemId(entity.getCartItemId())
                .productId(product.getProductId())
                .productTitle(product.getTitle())
                .coverImage(product.getCoverImage())
                .intent(entity.getIntent())
                .rentDays(entity.getRentDays())
                .lineTotal(lineTotal(entity, product))
                .addedAt(entity.getAddedAt())
                .build();
    }

    public CartResponse toResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(this::toItemResponse).toList();
        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .grandTotal(grandTotal)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private BigDecimal lineTotal(CartItem entity, Product product) {
        if (entity.getIntent() == CartItem.Intent.RENT) {
            BigDecimal rentRate = product.getRentRate() != null ? product.getRentRate() : BigDecimal.ZERO;
            int days = entity.getRentDays() != null ? entity.getRentDays() : 0;
            return rentRate.multiply(BigDecimal.valueOf(days));
        }
        return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
    }
}
