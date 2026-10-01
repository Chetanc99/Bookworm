package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.UserLibraryPackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserLibraryPackageResponse {
    private Long userLibraryPackageId;
    private Long userId;
    private Long libraryPackageId;
    private String packageName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UserLibraryPackage.Status status;
    private LocalDateTime purchasedAt;
}
