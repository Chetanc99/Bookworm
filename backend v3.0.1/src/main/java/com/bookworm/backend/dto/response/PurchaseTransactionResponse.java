package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.PurchaseTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PurchaseTransactionResponse {
    private Long purchaseTransactionId;
    private Long userId;
    private BigDecimal totalAmount;
    private PurchaseTransaction.Status status;
    private Integer loyaltyPointsEarned;
    private List<PurchaseItemResponse> items;
    private LocalDateTime createdAt;
}
