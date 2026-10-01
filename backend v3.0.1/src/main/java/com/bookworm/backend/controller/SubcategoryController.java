package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.SubcategoryRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.SubcategoryResponse;
import com.bookworm.backend.service.SubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subcategories")
@RequiredArgsConstructor
@Tag(name = "Product Subcategories", description = "Second-level catalog nodes under a category, e.g. Novel under eBooks")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    @GetMapping
    @Operation(summary = "List subcategories, optionally filtered by parent category")
    public ResponseEntity<ApiResponse<List<SubcategoryResponse>>> getAll(
            @RequestParam(required = false) Long categoryId) {
        List<SubcategoryResponse> result = categoryId != null
                ? subcategoryService.getByCategory(categoryId)
                : subcategoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{subcategoryId}")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> getById(@PathVariable Long subcategoryId) {
        return ResponseEntity.ok(ApiResponse.success(subcategoryService.getById(subcategoryId)));
    }

    @PostMapping
    @Operation(summary = "Create a subcategory (Admin CMS)")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> create(@Valid @RequestBody SubcategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subcategory created", subcategoryService.create(request)));
    }

    @PutMapping("/{subcategoryId}")
    @Operation(summary = "Update a subcategory (Admin CMS)")
    public ResponseEntity<ApiResponse<SubcategoryResponse>> update(
            @PathVariable Long subcategoryId, @Valid @RequestBody SubcategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Subcategory updated", subcategoryService.update(subcategoryId, request)));
    }

    @DeleteMapping("/{subcategoryId}")
    @Operation(summary = "Deactivate a subcategory (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long subcategoryId) {
        subcategoryService.delete(subcategoryId);
        return ResponseEntity.ok(ApiResponse.success("Subcategory deactivated", null));
    }
}
