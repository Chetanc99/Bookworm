package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LanguageResponse {
    private Long languageId;
    private String languageName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
