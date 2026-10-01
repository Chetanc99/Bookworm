package com.bookworm.notification.controller;

import com.bookworm.notification.dto.ApiResponse;
import com.bookworm.notification.dto.EmailNotificationRequest;
import com.bookworm.notification.dto.EmailNotificationResponse;
import com.bookworm.notification.service.EmailNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/** Send (or, if notification.mail.enabled=false, log) a single email, optionally with a base64-encoded attachment. */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    @PostMapping("/email")
    public ResponseEntity<ApiResponse<EmailNotificationResponse>> sendEmail(
            @Valid @RequestBody EmailNotificationRequest request) {
        emailNotificationService.send(request);
        EmailNotificationResponse response = EmailNotificationResponse.builder()
                .sent(true)
                .message("Notification accepted for delivery")
                .processedAt(Instant.now())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
