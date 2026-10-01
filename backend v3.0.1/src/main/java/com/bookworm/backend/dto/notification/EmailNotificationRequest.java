package com.bookworm.backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wire DTO for POST {bookworm.notification-service.base-url}/api/v1/notifications/email
 * on bookworm-notification-service. Deliberately duplicated rather than shared
 * via a common module - the two services are independently deployable/versioned,
 * same as any two real microservices talking over a REST contract; this is the
 * bookworm-backend-side copy of that contract, kept in sync by hand.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest {
    private String to;
    private String subject;
    private String body;
    private String attachmentFilename;
    private String attachmentContentType;
    private String attachmentBase64;
}
