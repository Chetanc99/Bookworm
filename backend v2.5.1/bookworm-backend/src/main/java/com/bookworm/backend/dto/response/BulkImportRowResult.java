package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BulkImportRowResult {
    private int rowNumber; // 1-based, matches the spreadsheet row (header = row 1)
    private boolean success;
    private String title;
    private Long productId; // null if the row failed
    private String errorMessage; // null if the row succeeded
}
