package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubcategoryRequest {

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotBlank(message = "Subcategory name is required")
    @Size(max = 100)
    private String subcategoryName;

    @Size(max = 255)
    private String description;

    private Integer sortOrder;

    private Boolean isActive;
}
