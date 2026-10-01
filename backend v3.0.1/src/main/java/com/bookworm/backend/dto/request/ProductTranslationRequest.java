package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductTranslationRequest {

    @NotNull(message = "languageId is required")
    private Long languageId;

    @NotBlank(message = "title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String shortDescription;

    private String description;
}
