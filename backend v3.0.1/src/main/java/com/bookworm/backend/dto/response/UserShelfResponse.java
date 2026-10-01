package com.bookworm.backend.dto.response;

import com.bookworm.backend.entity.UserShelf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * "My Shelf" row - what the user can currently open/read/play. source
 * distinguishes a permanent PURCHASE grant (expiresAt null) from a
 * time-limited RENT/LIBRARY grant.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserShelfResponse {
    private Long userShelfId;
    private Long productId;
    private String productTitle;
    private String coverImage;
    // Servable /uploads/** URL (see WebConfig) - permitAll static resource,
    // no separate streaming endpoint needed. Frontend can open this directly.
    private String filePath;
    private String fileType;
    private UserShelf.Source source;
    private LocalDateTime expiresAt;
    private LocalDateTime acquiredAt;
}
