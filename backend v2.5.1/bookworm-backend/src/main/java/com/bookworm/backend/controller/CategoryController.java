package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.CategoryRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.CategoryResponse;
import com.bookworm.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Product Categories", description = "Top-level catalog categories (eBooks, Audiobooks, Video Courses)")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "List categories (public browse - active only by default)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<CategoryResponse> result = activeOnly
                ? categoryService.getAllActive()
                : categoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a single category by id")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getById(categoryId)));
    }

    @PostMapping
    @Operation(summary = "Create a category (Admin CMS)")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", created));
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a category (Admin CMS)")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long categoryId, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.update(categoryId, request)));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Deactivate a category (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deactivated", null));
    }
}
