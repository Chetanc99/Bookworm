package com.bookworm.backend.dto.request;

import com.bookworm.backend.entity.CartItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "intent is required")
    private CartItem.Intent intent;

    // Required, and validated against the product's min_rent_days, when intent = RENT.
    @Min(value = 1, message = "rentDays must be at least 1")
    private Integer rentDays;
}
