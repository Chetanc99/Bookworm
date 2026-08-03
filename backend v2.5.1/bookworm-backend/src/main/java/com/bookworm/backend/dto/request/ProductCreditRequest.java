package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreditRequest {

    @NotNull(message = "creditTypeId is required")
    private Long creditTypeId;

    @NotBlank(message = "creditValue is required")
    @Size(max = 150)
    private String creditValue;
}
