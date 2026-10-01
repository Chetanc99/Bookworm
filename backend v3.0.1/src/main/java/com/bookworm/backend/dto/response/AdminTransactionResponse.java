package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unified row across PurchaseTransaction and RentTransaction for the admin
 * transactions report - see repository.AdminTransactionRepository, which
 * UNION ALLs both tables natively (there's no shared entity/table to query
 * with plain JPA here). transactionType is "PURCHASE" or "RENT"; status is
 * PurchaseTransaction.Status or RentTransaction.Status depending on type,
 * kept as a plain String since the two enums aren't the same type.
 */
@Getter
@Builder
@AllArgsConstructor
public class AdminTransactionResponse {
    private String transactionType;
    private Long transactionId;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
