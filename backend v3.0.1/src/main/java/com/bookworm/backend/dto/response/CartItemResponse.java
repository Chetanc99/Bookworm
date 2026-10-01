package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productTitle;
    private String coverImage;
    private CartItem.Intent intent;
    private Integer rentDays;

    // Line price: product.price for PURCHASE, product.rentRate * rentDays for RENT.
    private BigDecimal lineTotal;

    private LocalDateTime addedAt;
}
