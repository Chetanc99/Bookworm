package com.bookworm.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailNotificationRequest {

    @NotBlank(message = "to is required")
    @Email(message = "to must be a valid email address")
    private String to;

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "body is required")
    private String body;

    // All three optional together - a plain-text notification has none of them.
    // If attachmentBase64 is present, attachmentFilename and attachmentContentType
    // must be too (validated in the service, not here - this DTO stays a plain
    // data holder like the rest of this codebase's request DTOs).
    private String attachmentFilename;
    private String attachmentContentType;
    private String attachmentBase64;
}
