package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.AdminTransactionResponse;
import com.bookworm.backend.dto.response.RevenueSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminTransactionService {

    /** Admin-wide Purchase + Rent transaction listing, most recent first. */
    Page<AdminTransactionResponse> getAll(Pageable pageable);

    /** Total revenue + transaction counts across Purchase + Rent. */
    RevenueSummaryResponse getRevenueSummary();
}
