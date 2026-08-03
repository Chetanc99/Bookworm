package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.LanguageRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.LanguageResponse;
import com.bookworm.backend.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
@Tag(name = "Languages", description = "Product languages; also the foundation for future multilingual UI support")
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LanguageResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<LanguageResponse> result = activeOnly ? languageService.getAllActive() : languageService.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{languageId}")
    public ResponseEntity<ApiResponse<LanguageResponse>> getById(@PathVariable Long languageId) {
        return ResponseEntity.ok(ApiResponse.success(languageService.getById(languageId)));
    }

    @PostMapping
    @Operation(summary = "Create a language (Admin CMS)")
    public ResponseEntity<ApiResponse<LanguageResponse>> create(@Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Language created", languageService.create(request)));
    }

    @PutMapping("/{languageId}")
    @Operation(summary = "Update a language (Admin CMS)")
    public ResponseEntity<ApiResponse<LanguageResponse>> update(
            @PathVariable Long languageId, @Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Language updated", languageService.update(languageId, request)));
    }

    @DeleteMapping("/{languageId}")
    @Operation(summary = "Deactivate a language (soft delete, Admin CMS)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long languageId) {
        languageService.delete(languageId);
        return ResponseEntity.ok(ApiResponse.success("Language deactivated", null));
    }
}
