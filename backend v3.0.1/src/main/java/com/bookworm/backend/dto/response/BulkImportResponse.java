package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BulkImportResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<BulkImportRowResult> results;
}
