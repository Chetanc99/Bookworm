package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.dto.response.RoyaltyLedgerResponse;
import com.bookworm.backend.dto.response.RoyaltySummaryResponse;
import com.bookworm.backend.service.RoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/beneficiaries/{beneficiaryId}/royalties")
@RequiredArgsConstructor
@Tag(name = "Royalty Ledger", description = "Per-beneficiary royalty accrual from Purchase, Rent, and Library-borrow revenue")
public class RoyaltyController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RoyaltyService royaltyService;

    @GetMapping
    @Operation(summary = "Royalty ledger entries for a beneficiary, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<RoyaltyLedgerResponse>>> getHistory(
            @PathVariable Long beneficiaryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        Page<RoyaltyLedgerResponse> result = royaltyService.getHistory(beneficiaryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Total royalty earned to date for a beneficiary")
    public ResponseEntity<ApiResponse<RoyaltySummaryResponse>> getSummary(@PathVariable Long beneficiaryId) {
        return ResponseEntity.ok(ApiResponse.success(royaltyService.getSummary(beneficiaryId)));
    }
}
