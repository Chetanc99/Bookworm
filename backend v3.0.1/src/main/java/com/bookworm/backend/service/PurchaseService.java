package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseService {

    /**
     * Checks out every PURCHASE-intent item currently in the user's cart:
     * creates the transaction + line items, grants permanent UserShelf
     * access, awards loyalty points, then clears those items from the cart.
     */
    PurchaseTransactionResponse checkout(Long userId);

    Page<PurchaseTransactionResponse> getHistory(Long userId, Pageable pageable);

    PurchaseTransactionResponse getById(Long userId, Long purchaseTransactionId);
}
