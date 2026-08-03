package com.bookworm.backend.service.impl;

import com.bookworm.backend.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * No SMTP server is configured for local/dev by default (spring.mail.host is
 * blank), so bookworm.mail.enabled defaults to false and this just logs what
 * would have been sent - keeps the forgot-password/receipt flows fully
 * testable without real infra. Set bookworm.mail.enabled=true and the
 * spring.mail.* properties in any environment with a real SMTP account
 * (e.g. via env vars) to switch on real delivery - no code change needed.
 *
 * Backs off (via @ConditionalOnProperty) when bookworm.notification-service.enabled=true,
 * in which case RemoteNotificationMailServiceImpl is registered instead - exactly one
 * MailService bean exists at a time, so every caller's @Autowired MailService still
 * resolves unambiguously either way.
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "bookworm.notification-service", name = "enabled",
        havingValue = "false", matchIfMissing = true)
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromAddress;

    public MailServiceImpl(
            JavaMailSender mailSender,
            @Value("${bookworm.mail.enabled:false}") boolean enabled,
            @Value("${bookworm.mail.from:no-reply@bookworm.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    @Override
    public void send(String to, String subject, String body) {
        if (!enabled) {
            log.info("[MAIL DISABLED - would send] to={} subject='{}' body='{}'", to, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendWithAttachment(String to, String subject, String body,
                                    String attachmentFilename, byte[] attachmentBytes, String attachmentContentType) {
        if (!enabled) {
            log.info("[MAIL DISABLED - would send] to={} subject='{}' attachment={} ({} bytes)",
                    to, subject, attachmentFilename, attachmentBytes != null ? attachmentBytes.length : 0);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // multipart=true is required for addAttachment() to work - a plain MimeMessageHelper
            // without it silently ignores attachments.
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(attachmentFilename,
                    new org.springframework.core.io.ByteArrayResource(attachmentBytes), attachmentContentType);
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            // Wrapped the same way Spring's own JavaMailSenderImpl wraps checked
            // MessagingExceptions, so callers only ever need to catch MailException.
            throw new MailSendException("Failed to build email with attachment for " + to, ex);
        }
    }
}
