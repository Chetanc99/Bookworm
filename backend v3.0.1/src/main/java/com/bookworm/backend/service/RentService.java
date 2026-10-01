package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.RentTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RentService {

    /**
     * Checks out every RENT-intent item in the user's cart: one
     * RentTransaction per product, each with its own start/end window, plus
     * a time-limited UserShelf grant. Returns one response per product rented.
     */
    List<RentTransactionResponse> checkout(Long userId);

    Page<RentTransactionResponse> getHistory(Long userId, Pageable pageable);

    RentTransactionResponse getById(Long userId, Long rentTransactionId);
}
