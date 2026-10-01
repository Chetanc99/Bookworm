package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.ProductStakeholderRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.ProductStakeholderResponse;
import com.bookworm.backend.service.ProductStakeholderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/stakeholders")
@RequiredArgsConstructor
@Tag(name = "Product Stakeholder Credits", description = "Attribution credits (Author, Publisher, Director...) attached to a product")
public class ProductStakeholderController {

    private final ProductStakeholderService productStakeholderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductStakeholderResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productStakeholderService.getByProduct(productId)));
    }

    @PostMapping
    @Operation(summary = "Credit a stakeholder on this product (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductStakeholderResponse>> addCredit(
            @PathVariable Long productId, @Valid @RequestBody ProductStakeholderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stakeholder credited", productStakeholderService.addCredit(productId, request)));
    }

    @PutMapping("/{productStakeholderId}")
    @Operation(summary = "Edit an existing stakeholder credit's stakeholder/role in place (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductStakeholderResponse>> updateCredit(
            @PathVariable Long productId, @PathVariable Long productStakeholderId,
            @Valid @RequestBody ProductStakeholderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Credit updated", productStakeholderService.updateCredit(productId, productStakeholderId, request)));
    }

    @DeleteMapping("/{productStakeholderId}")
    @Operation(summary = "Remove a stakeholder credit from this product (Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> removeCredit(
            @PathVariable Long productId, @PathVariable Long productStakeholderId) {
        productStakeholderService.removeCredit(productId, productStakeholderId);
        return ResponseEntity.ok(ApiResponse.success("Credit removed", null));
    }
}
