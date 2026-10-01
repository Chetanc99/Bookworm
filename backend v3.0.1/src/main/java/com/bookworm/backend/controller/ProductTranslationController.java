package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.ProductTranslationRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.ProductTranslationResponse;
import com.bookworm.backend.service.ProductTranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/translations")
@RequiredArgsConstructor
@Tag(name = "Product Translations", description = "Alternate-language title/description overlays for bilingual catalog display")
public class ProductTranslationController {

    private final ProductTranslationService translationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductTranslationResponse>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(translationService.getByProduct(productId)));
    }

    @PutMapping
    @Operation(summary = "Add or replace this product's translation for one language (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductTranslationResponse>> upsert(
            @PathVariable Long productId, @Valid @RequestBody ProductTranslationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Translation saved", translationService.upsert(productId, request)));
    }

    @DeleteMapping("/{languageId}")
    @Operation(summary = "Remove this product's translation for one language (Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId, @PathVariable Long languageId) {
        translationService.remove(productId, languageId);
        return ResponseEntity.ok(ApiResponse.success("Translation removed", null));
    }
}
