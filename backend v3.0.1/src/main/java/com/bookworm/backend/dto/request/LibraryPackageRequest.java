package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryPackageRequest {

    @NotBlank(message = "packageName is required")
    private String packageName;

    private String description;

    @NotNull(message = "price is required")
    private BigDecimal price;

    @NotNull(message = "durationDays is required")
    @Min(value = 1, message = "durationDays must be at least 1")
    private Integer durationDays;

    @Min(value = 1, message = "maxConcurrentBorrows must be at least 1")
    private Integer maxConcurrentBorrows;
}
