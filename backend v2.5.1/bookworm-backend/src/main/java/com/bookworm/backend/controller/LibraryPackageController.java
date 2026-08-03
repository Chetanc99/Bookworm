package com.bookworm.backend.controller;

import com.bookworm.backend.dto.request.LibraryPackageRequest;
import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.LibraryPackageResponse;
import com.bookworm.backend.service.LibraryPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library-packages")
@RequiredArgsConstructor
@Tag(name = "Library Packages", description = "Subscription tiers granting borrow access to library-eligible products")
public class LibraryPackageController {

    private final LibraryPackageService service;

    @GetMapping
    @Operation(summary = "List active library packages")
    public ResponseEntity<ApiResponse<List<LibraryPackageResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllActive()));
    }

    @GetMapping("/{libraryPackageId}")
    @Operation(summary = "Get a library package by id")
    public ResponseEntity<ApiResponse<LibraryPackageResponse>> getById(@PathVariable Long libraryPackageId) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(libraryPackageId)));
    }

    @PostMapping
    @Operation(summary = "Create a library package (Admin CMS)")
    public ResponseEntity<ApiResponse<LibraryPackageResponse>> create(@Valid @RequestBody LibraryPackageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Library package created", service.create(request)));
    }

    @PutMapping("/{libraryPackageId}")
    @Operation(summary = "Update a library package (Admin CMS)")
    public ResponseEntity<ApiResponse<LibraryPackageResponse>> update(
            @PathVariable Long libraryPackageId, @Valid @RequestBody LibraryPackageRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Library package updated", service.update(libraryPackageId, request)));
    }

    @DeleteMapping("/{libraryPackageId}")
    @Operation(summary = "Deactivate a library package (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long libraryPackageId) {
        service.delete(libraryPackageId);
        return ResponseEntity.ok(ApiResponse.success("Library package deactivated", null));
    }
}
