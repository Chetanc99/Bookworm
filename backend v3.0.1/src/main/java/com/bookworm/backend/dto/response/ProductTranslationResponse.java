package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductTranslationResponse {
    private Long productTranslationId;
    private Long languageId;
    private String languageName;
    private String title;
    private String shortDescription;
    private String description;
}
