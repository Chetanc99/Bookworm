package com.bookworm.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class EmailNotificationResponse {
    private boolean sent;
    private String message;
    private Instant processedAt;
}
