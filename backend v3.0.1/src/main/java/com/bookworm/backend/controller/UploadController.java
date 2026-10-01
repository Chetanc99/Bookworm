package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.UploadResponse;
import com.bookworm.backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin CMS file uploads (product cover images / content files). Returns a
 * URL to plug into ProductRequest.coverImage / .filePath - this endpoint
 * does not itself touch the Product entity, keeping upload and product-CRUD
 * concerns separate.
 */
@RestController
@RequestMapping("/api/v1/admin/uploads")
@RequiredArgsConstructor
@Tag(name = "Admin - Uploads", description = "Cover image / content file uploads (ADMIN only)")
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    @Operation(summary = "Upload a product cover image, get back a servable URL")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file, "covers");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image uploaded", UploadResponse.builder().url(url).build()));
    }

    @PostMapping(value = "/content", consumes = "multipart/form-data")
    @Operation(summary = "Upload a product content file (eBook/audio/video), get back a servable URL")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadContent(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file, "content");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded", UploadResponse.builder().url(url).build()));
    }
}
