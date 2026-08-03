package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageRequest {

    @NotBlank(message = "Language name is required")
    @Size(max = 50)
    private String languageName;

    private Boolean isActive;
}
