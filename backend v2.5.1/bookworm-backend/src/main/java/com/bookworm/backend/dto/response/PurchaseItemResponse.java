package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class PurchaseItemResponse {
    private Long purchaseItemId;
    private Long productId;
    private String productTitle;
    private BigDecimal unitPrice;
}
