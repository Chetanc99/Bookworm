package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.notification.EmailNotificationRequest;
import com.bookworm.backend.dto.notification.NotificationApiResponse;
import com.bookworm.backend.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Base64;

/**
 * Delegates outbound mail to bookworm-notification-service over REST instead
 * of sending it locally - demonstrates the microservice split described in
 * that module's pom.xml. Implements the same MailService interface as
 * MailServiceImpl so every existing call site (PurchaseServiceImpl,
 * PasswordResetServiceImpl, OtpServiceImpl, EmailVerificationServiceImpl)
 * needs zero changes regardless of which one is active - only one of the two
 * is ever registered, chosen by bookworm.notification-service.enabled (see
 * @ConditionalOnProperty on both this class and MailServiceImpl).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "bookworm.notification-service", name = "enabled", havingValue = "true")
public class RemoteNotificationMailServiceImpl implements MailService {

    private final RestClient notificationServiceRestClient;

    @Override
    public void send(String to, String subject, String body) {
        post(EmailNotificationRequest.builder().to(to).subject(subject).body(body).build());
    }

    @Override
    public void sendWithAttachment(String to, String subject, String body,
                                    String attachmentFilename, byte[] attachmentBytes, String attachmentContentType) {
        post(EmailNotificationRequest.builder()
                .to(to).subject(subject).body(body)
                .attachmentFilename(attachmentFilename)
                .attachmentContentType(attachmentContentType)
                .attachmentBase64(Base64.getEncoder().encodeToString(attachmentBytes))
                .build());
    }

    private void post(EmailNotificationRequest request) {
        try {
            NotificationApiResponse response = notificationServiceRestClient.post()
                    .uri("/api/v1/notifications/email")
                    .body(request)
                    .retrieve()
                    .body(NotificationApiResponse.class);

            if (response == null || !response.isSuccess()) {
                throw new MailSendException("bookworm-notification-service rejected the request"
                        + (response != null ? ": " + response.getMessage() : ""));
            }
        } catch (RestClientException ex) {
            // Same exception type MailServiceImpl's own failure path would throw, so
            // every caller's existing try/catch (e.g. PurchaseServiceImpl's best-effort
            // receipt email) keeps working unchanged regardless of which MailService
            // implementation is wired in.
            throw new MailSendException("Failed to reach bookworm-notification-service for " + request.getTo(), ex);
        }
    }
}
