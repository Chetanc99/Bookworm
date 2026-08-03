package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BeneficiaryTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull(message = "defaultRoyaltyPercentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Default royalty percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Default royalty percentage cannot exceed 100")
    private BigDecimal defaultRoyaltyPercentage;

    private Boolean isActive;
}
