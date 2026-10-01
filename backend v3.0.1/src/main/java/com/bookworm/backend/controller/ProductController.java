package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.PageResponse;
import com.bookworm.backend.dto.response.ProductResponse;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Core catalog: browse, search, and Admin CMS management of eBooks/Audiobooks/Video Courses")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Browse/search products. Filters support subcategoryId, genreId, languageId, isRentable, "
            + "mediaType (BOOK/AUDIOBOOK/VIDEO_COURSE/PODCAST), and keyword (matches base title AND any translation "
            + "title - bilingual search). displayLanguageId overlays a ProductTranslation onto each result if one "
            + "exists for that language.")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> browse(
            @RequestParam(required = false) Long subcategoryId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Long languageId,
            @RequestParam(required = false) Boolean isRentable,
            @RequestParam(required = false) Product.MediaType mediaType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long displayLanguageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("productId").descending());
        Page<ProductResponse> result = productService.browse(
                subcategoryId, genreId, languageId, isRentable, mediaType, keyword, displayLanguageId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get full product detail. displayLanguageId overlays a ProductTranslation if one exists for that language.")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @PathVariable Long productId, @RequestParam(required = false) Long displayLanguageId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(productId, displayLanguageId)));
    }

    @PostMapping
    @Operation(summary = "Create a product (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.create(request)));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update a product (Admin CMS)")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long productId, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.update(productId, request)));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Pull a product off the storefront (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated", null));
    }
}
