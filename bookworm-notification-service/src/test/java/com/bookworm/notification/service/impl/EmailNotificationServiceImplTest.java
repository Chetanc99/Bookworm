package com.bookworm.notification.service.impl;

import com.bookworm.notification.dto.EmailNotificationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EmailNotificationServiceImplTest {

    private final JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);

    @Test
    void send_disabled_neverTouchesMailSender() {
        EmailNotificationServiceImpl service = new EmailNotificationServiceImpl(mailSender, false, "no-reply@bookworm.com");

        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setTo("user@example.com");
        request.setSubject("Hi");
        request.setBody("Body");

        service.send(request);

        verify(mailSender, never()).send(Mockito.any(org.springframework.mail.SimpleMailMessage.class));
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    void send_attachmentBase64WithoutFilename_throwsIllegalArgument() {
        EmailNotificationServiceImpl service = new EmailNotificationServiceImpl(mailSender, true, "no-reply@bookworm.com");

        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setTo("user@example.com");
        request.setSubject("Hi");
        request.setBody("Body");
        request.setAttachmentBase64("Zm9v"); // "foo"
        // attachmentFilename / attachmentContentType deliberately left unset

        assertThatThrownBy(() -> service.send(request)).isInstanceOf(IllegalArgumentException.class);
    }
}
