package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.ProductBeneficiaryRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.ProductBeneficiaryResponse;
import com.bookworm.backend.service.ProductBeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Product Royalty Splits", description = "Royalty percentage allocations to Beneficiaries for a product; total must not exceed 100%")
public class ProductBeneficiaryController {

    private final ProductBeneficiaryService productBeneficiaryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductBeneficiaryResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productBeneficiaryService.getByProduct(productId)));
    }

    @PostMapping
    @Operation(summary = "Add a royalty split to this product (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductBeneficiaryResponse>> addSplit(
            @PathVariable Long productId, @Valid @RequestBody ProductBeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Royalty split added", productBeneficiaryService.addSplit(productId, request)));
    }

    @PutMapping
    @Operation(summary = "Replace this product's entire beneficiary/royalty allocation in one call (Admin CMS). "
            + "Never affects historical RoyaltyLedger rows - see ProductBeneficiaryServiceImpl.replaceAssignments.")
    public ResponseEntity<ApiResponse<List<ProductBeneficiaryResponse>>> replaceAssignments(
            @PathVariable Long productId, @Valid @RequestBody List<@Valid ProductBeneficiaryRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(
                "Royalty allocation updated", productBeneficiaryService.replaceAssignments(productId, requests)));
    }

    @DeleteMapping("/{productBeneficiaryId}")
    @Operation(summary = "Remove a royalty split from this product (Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> removeSplit(
            @PathVariable Long productId, @PathVariable Long productBeneficiaryId) {
        productBeneficiaryService.removeSplit(productId, productBeneficiaryId);
        return ResponseEntity.ok(ApiResponse.success("Royalty split removed", null));
    }
}
