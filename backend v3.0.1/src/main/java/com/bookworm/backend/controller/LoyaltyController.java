package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.LoyaltyBalanceResponse;
import com.bookworm.backend.dto.response.LoyaltyLedgerResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty Ledger", description = "Points earned from purchases (see PurchaseServiceImpl) - balance and history")
public class LoyaltyController {

    private static final int MAX_PAGE_SIZE = 100;

    private final LoyaltyService loyaltyService;

    @GetMapping("/balance")
    @Operation(summary = "Get the user's current loyalty point balance")
    public ResponseEntity<ApiResponse<LoyaltyBalanceResponse>> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getBalance(userId)));
    }

    @GetMapping
    @Operation(summary = "Loyalty ledger entries for the user, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<LoyaltyLedgerResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        Page<LoyaltyLedgerResponse> result = loyaltyService.getHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }
}
