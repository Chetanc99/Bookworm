package com.bookworm.backend.controller;

import com.bookworm.backend.dto.response.ApiResponse;
import com.bookworm.backend.dto.response.BulkImportResponse;
import com.bookworm.backend.service.ProductBulkImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * See ProductBulkImportServiceImpl for the expected .xlsx column headers.
 */
@RestController
@RequestMapping("/api/v1/admin/products/bulk-import")
@RequiredArgsConstructor
@Tag(name = "Admin - Bulk Import", description = "Excel bulk product import (ADMIN only)")
public class ProductBulkImportController {

    private final ProductBulkImportService productBulkImportService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Bulk-create products from an .xlsx workbook (one row per product)")
    public ResponseEntity<ApiResponse<BulkImportResponse>> importProducts(@RequestParam("file") MultipartFile file) {
        BulkImportResponse result = productBulkImportService.importProducts(file);
        return ResponseEntity.ok(ApiResponse.success(
                result.getFailureCount() == 0 ? "Import completed successfully" : "Import completed with some failures",
                result));
    }
}
