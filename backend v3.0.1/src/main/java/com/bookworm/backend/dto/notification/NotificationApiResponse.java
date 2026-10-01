package com.bookworm.backend.dto.notification;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Deserialization target for bookworm-notification-service's ApiResponse
 * envelope - only the fields RemoteNotificationMailServiceImpl actually
 * reads (Jackson ignores the rest by default).
 */
@Getter
@NoArgsConstructor
public class NotificationApiResponse {
    private boolean success;
    private String message;
}
