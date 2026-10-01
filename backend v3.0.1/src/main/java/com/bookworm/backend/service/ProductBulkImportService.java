package com.bookworm.backend.service;

import com.bookworm.backend.dto.response.BulkImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductBulkImportService {

    /**
     * Parses an .xlsx workbook (Apache POI - already a pom.xml dependency,
     * previously unused anywhere) and creates one Product per data row via
     * the existing ProductService.create, so every existing validation rule
     * (rentable requires rentRate/minRentDays, etc.) is reused rather than
     * duplicated. One bad row does not abort the rest of the batch - see
     * BulkImportResponse for the per-row outcome.
     */
    BulkImportResponse importProducts(MultipartFile file);
}
