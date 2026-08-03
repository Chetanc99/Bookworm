package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.UserLibrary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserLibraryResponse {
    private Long userLibraryId;
    private Long productId;
    private String productTitle;
    // Servable /uploads/** URL (see WebConfig) - only meaningful while the
    // borrow is ACTIVE; frontend should not offer Read for RETURNED/EXPIRED rows.
    private String filePath;
    private String fileType;
    private LocalDateTime borrowedAt;
    private LocalDateTime dueDate;
    private UserLibrary.Status status;
}
