package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.AdminTransactionResponse;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.dto.response.RevenueSummaryResponse;
import com.bookworm.backend.service.AdminTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
@Tag(name = "Admin - Transactions", description = "Admin-wide Purchase + Rent transaction listing (ADMIN only)")
public class AdminTransactionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminTransactionService adminTransactionService;

    @GetMapping
    @Operation(summary = "List all Purchase and Rent transactions across every user, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<AdminTransactionResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        Page<AdminTransactionResponse> result = adminTransactionService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Total revenue + Purchase/Rent counts across all users")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> getRevenueSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminTransactionService.getRevenueSummary()));
    }
}
