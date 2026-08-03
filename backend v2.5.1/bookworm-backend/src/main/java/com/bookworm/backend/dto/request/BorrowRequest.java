package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "borrowDays is required")
    @Min(value = 1, message = "borrowDays must be at least 1")
    private Integer borrowDays;
}
