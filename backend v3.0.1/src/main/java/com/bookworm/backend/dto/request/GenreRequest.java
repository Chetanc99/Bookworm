package com.bookworm.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenreRequest {

    @NotNull(message = "subcategoryId is required")
    private Long subcategoryId;

    @NotBlank(message = "Genre name is required")
    @Size(max = 100)
    private String genreName;

    private Boolean isActive;
}
