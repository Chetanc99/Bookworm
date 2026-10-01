package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.GenreRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.GenreResponse;
import com.bookworm.backend.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Third-level catalog nodes under a subcategory, e.g. Sci-fi under Novel")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAll(
            @RequestParam(required = false) Long subcategoryId) {
        List<GenreResponse> result = subcategoryId != null
                ? genreService.getBySubcategory(subcategoryId)
                : genreService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<ApiResponse<GenreResponse>> getById(@PathVariable Long genreId) {
        return ResponseEntity.ok(ApiResponse.success(genreService.getById(genreId)));
    }

    @PostMapping
    @Operation(summary = "Create a genre (Admin CMS)")
    public ResponseEntity<ApiResponse<GenreResponse>> create(@Valid @RequestBody GenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Genre created", genreService.create(request)));
    }

    @PutMapping("/{genreId}")
    @Operation(summary = "Update a genre (Admin CMS)")
    public ResponseEntity<ApiResponse<GenreResponse>> update(
            @PathVariable Long genreId, @Valid @RequestBody GenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Genre updated", genreService.update(genreId, request)));
    }

    @DeleteMapping("/{genreId}")
    @Operation(summary = "Deactivate a genre (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long genreId) {
        genreService.delete(genreId);
        return ResponseEntity.ok(ApiResponse.success("Genre deactivated", null));
    }
}
