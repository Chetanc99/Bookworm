package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.RentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RentTransactionResponse {
    private Long rentTransactionId;
    private Long userId;
    private Long productId;
    private String productTitle;
    private Integer rentDays;
    private BigDecimal rentRate;
    private BigDecimal totalAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private RentTransaction.Status status;
    private LocalDateTime createdAt;
}
