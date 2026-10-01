package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import com.bookworm.backend.service.InvoiceService;
import com.bookworm.backend.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchase", description = "Checkout of PURCHASE-intent cart items; grants shelf access and loyalty points")
public class PurchaseController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PurchaseService purchaseService;
    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "Check out all PURCHASE-intent items currently in the user's cart")
    public ResponseEntity<ApiResponse<PurchaseTransactionResponse>> checkout(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase completed", purchaseService.checkout(userId)));
    }

    @GetMapping
    @Operation(summary = "Purchase history for the user, most recent first")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseTransactionResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
        Page<PurchaseTransactionResponse> result = purchaseService.getHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{purchaseTransactionId}")
    @Operation(summary = "Get a single purchase transaction (receipt) by id")
    public ResponseEntity<ApiResponse<PurchaseTransactionResponse>> getById(
            @PathVariable Long userId, @PathVariable Long purchaseTransactionId) {
        return ResponseEntity.ok(ApiResponse.success(purchaseService.getById(userId, purchaseTransactionId)));
    }

    @GetMapping("/{purchaseTransactionId}/invoice")
    @Operation(summary = "Download a PDF invoice for a purchase transaction")
    public ResponseEntity<byte[]> getInvoice(@PathVariable Long userId, @PathVariable Long purchaseTransactionId) {
        byte[] pdf = invoiceService.generatePurchaseInvoice(userId, purchaseTransactionId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"bookworm-invoice-" + purchaseTransactionId + ".pdf\"")
                .body(pdf);
    }
}
