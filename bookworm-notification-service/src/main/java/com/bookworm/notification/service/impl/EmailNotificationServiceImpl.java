package com.bookworm.notification.service.impl;

import com.bookworm.notification.dto.EmailNotificationRequest;
import com.bookworm.notification.exception.NotificationDeliveryException;
import com.bookworm.notification.service.EmailNotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * Mirrors bookworm-backend's MailServiceImpl on/off-switch pattern
 * (notification.mail.enabled, default false) so this service is runnable
 * and testable with no real SMTP account configured, same reasoning as the
 * monolith's own MailServiceImpl.
 */
@Service
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public EmailNotificationServiceImpl(
            JavaMailSender mailSender,
            @Value("${notification.mail.enabled:false}") boolean enabled,
            @Value("${notification.mail.from:no-reply@bookworm.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(EmailNotificationRequest request) {
        boolean hasAttachment = StringUtils.hasText(request.getAttachmentBase64());

        if (!enabled) {
            log.info("[NOTIFICATION MAIL DISABLED - would send] to={} subject='{}' attachment={}",
                    request.getTo(), request.getSubject(),
                    hasAttachment ? request.getAttachmentFilename() : "none");
            return;
        }

        try {
            if (!hasAttachment) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(request.getTo());
                message.setSubject(request.getSubject());
                message.setText(request.getBody());
                mailSender.send(message);
                return;
            }

            if (!StringUtils.hasText(request.getAttachmentFilename())
                    || !StringUtils.hasText(request.getAttachmentContentType())) {
                throw new IllegalArgumentException(
                        "attachmentFilename and attachmentContentType are required when attachmentBase64 is present");
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromAddress);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody());
            byte[] attachmentBytes = Base64.getDecoder().decode(request.getAttachmentBase64());
            helper.addAttachment(request.getAttachmentFilename(),
                    new ByteArrayResource(attachmentBytes), request.getAttachmentContentType());
            mailSender.send(mimeMessage);
        } catch (IllegalArgumentException ex) {
            throw ex; // let GlobalExceptionHandler map this to 400, not 502
        } catch (Exception ex) {
            throw new NotificationDeliveryException("Failed to deliver email to " + request.getTo(), ex);
        }
    }
}
