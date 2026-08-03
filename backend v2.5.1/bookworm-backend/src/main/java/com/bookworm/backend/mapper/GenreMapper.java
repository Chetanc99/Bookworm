package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.GenreResponse;
import com.bookworm.backend.entity.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {

    public GenreResponse toResponse(Genre entity) {
        return GenreResponse.builder()
                .genreId(entity.getGenreId())
                .subcategoryId(entity.getSubcategory().getSubcategoryId())
                .subcategoryName(entity.getSubcategory().getSubcategoryName())
                .genreName(entity.getGenreName())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
