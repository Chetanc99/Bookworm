package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.LibraryPackageResponse;
import com.bookworm.backend.entity.LibraryPackage;
import org.springframework.stereotype.Component;

@Component
public class LibraryPackageMapper {

    public LibraryPackageResponse toResponse(LibraryPackage entity) {
        return LibraryPackageResponse.builder()
                .libraryPackageId(entity.getLibraryPackageId())
                .packageName(entity.getPackageName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .durationDays(entity.getDurationDays())
                .maxConcurrentBorrows(entity.getMaxConcurrentBorrows())
                .isActive(entity.getIsActive())
                .build();
    }
}
