package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.PurchaseItemResponse;
import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import com.bookworm.backend.entity.PurchaseItem;
import com.bookworm.backend.entity.PurchaseTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PurchaseMapper {

    public PurchaseItemResponse toItemResponse(PurchaseItem entity) {
        return PurchaseItemResponse.builder()
                .purchaseItemId(entity.getPurchaseItemId())
                .productId(entity.getProduct().getProductId())
                .productTitle(entity.getProduct().getTitle())
                .unitPrice(entity.getUnitPrice())
                .build();
    }

    public PurchaseTransactionResponse toResponse(
            PurchaseTransaction entity, List<PurchaseItem> items, int loyaltyPointsEarned) {
        return PurchaseTransactionResponse.builder()
                .purchaseTransactionId(entity.getPurchaseTransactionId())
                .userId(entity.getUserId())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .loyaltyPointsEarned(loyaltyPointsEarned)
                .items(items.stream().map(this::toItemResponse).toList())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
