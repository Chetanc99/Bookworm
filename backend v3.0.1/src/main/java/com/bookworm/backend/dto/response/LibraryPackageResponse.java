package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class LibraryPackageResponse {
    private Long libraryPackageId;
    private String packageName;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private Integer maxConcurrentBorrows;
    private Boolean isActive;
}
