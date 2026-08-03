package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeneficiaryRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;

    // Optional - purely for defaulting a percentage when this beneficiary is
    // later added to a product's royalty split. Null is valid: not every
    // beneficiary fits a preset.
    private Long beneficiaryTypeId;

    private Boolean isActive;
}
