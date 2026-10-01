package com.bookworm.notification.service;

import com.bookworm.notification.dto.EmailNotificationRequest;

public interface EmailNotificationService {
    /** Sends (or, when disabled, logs) the email described by request. */
    void send(EmailNotificationRequest request);
}
