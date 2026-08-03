package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.LanguageResponse;
import com.bookworm.backend.entity.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageMapper {

    public LanguageResponse toResponse(Language entity) {
        return LanguageResponse.builder()
                .languageId(entity.getLanguageId())
                .languageName(entity.getLanguageName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
