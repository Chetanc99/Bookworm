package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.ProductCreditRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.ProductCreditResponse;
import com.bookworm.backend.service.ProductCreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/credits")
@RequiredArgsConstructor
@Tag(name = "Product Credits", description = "Free-text role credits (Translator: X, Narrator: Y...) attached to a product")
public class ProductCreditController {

    private final ProductCreditService productCreditService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCreditResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productCreditService.getByProduct(productId)));
    }

    @PostMapping
    @Operation(summary = "Add a credit to this product (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductCreditResponse>> addCredit(
            @PathVariable Long productId, @Valid @RequestBody ProductCreditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit added", productCreditService.addCredit(productId, request)));
    }

    @DeleteMapping("/{productCreditId}")
    @Operation(summary = "Remove a credit from this product (Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> removeCredit(
            @PathVariable Long productId, @PathVariable Long productCreditId) {
        productCreditService.removeCredit(productId, productCreditId);
        return ResponseEntity.ok(ApiResponse.success("Credit removed", null));
    }
}
