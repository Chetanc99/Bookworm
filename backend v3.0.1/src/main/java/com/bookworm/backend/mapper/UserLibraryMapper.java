package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.UserLibraryPackageResponse;
import com.bookworm.backend.dto.response.UserLibraryResponse;
import com.bookworm.backend.entity.UserLibrary;
import com.bookworm.backend.entity.UserLibraryPackage;
import org.springframework.stereotype.Component;

@Component
public class UserLibraryMapper {

    public UserLibraryPackageResponse toSubscriptionResponse(UserLibraryPackage entity) {
        return UserLibraryPackageResponse.builder()
                .userLibraryPackageId(entity.getUserLibraryPackageId())
                .userId(entity.getUserId())
                .libraryPackageId(entity.getLibraryPackage().getLibraryPackageId())
                .packageName(entity.getLibraryPackage().getPackageName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .purchasedAt(entity.getPurchasedAt())
                .build();
    }

    public UserLibraryResponse toBorrowResponse(UserLibrary entity) {
        return UserLibraryResponse.builder()
                .userLibraryId(entity.getUserLibraryId())
                .productId(entity.getProduct().getProductId())
                .productTitle(entity.getProduct().getTitle())
                .filePath(entity.getProduct().getFilePath())
                .fileType(entity.getProduct().getFileType())
                .borrowedAt(entity.getBorrowedAt())
                .dueDate(entity.getDueDate())
                .status(entity.getStatus())
                .build();
    }
}
