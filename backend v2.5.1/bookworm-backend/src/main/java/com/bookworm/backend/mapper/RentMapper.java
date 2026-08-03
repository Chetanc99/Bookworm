package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.RentTransactionResponse;
import com.bookworm.backend.entity.RentTransaction;
import org.springframework.stereotype.Component;

@Component
public class RentMapper {

    public RentTransactionResponse toResponse(RentTransaction entity) {
        return RentTransactionResponse.builder()
                .rentTransactionId(entity.getRentTransactionId())
                .userId(entity.getUserId())
                .productId(entity.getProduct().getProductId())
                .productTitle(entity.getProduct().getTitle())
                .rentDays(entity.getRentDays())
                .rentRate(entity.getRentRate())
                .totalAmount(entity.getTotalAmount())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
