package com.bookworm.backend.service;

public interface MailService {

    /**
     * Sends a plain-text email, or logs it instead when bookworm.mail.enabled
     * is false (default - see MailServiceImpl). Generic on purpose: reused
     * by password reset today, and by email verification / purchase receipts
     * once those land, without needing a new service each time.
     */
    void send(String to, String subject, String body);

    /**
     * Same disabled/logging behavior as send(), plus a single file attachment
     * (e.g. a PDF invoice). Kept as a separate method rather than overloading
     * send() with nullable attachment params, since a MimeMessage is a
     * heavier construct than SimpleMailMessage and most callers (OTP, password
     * reset, email verification) never need one.
     */
    void sendWithAttachment(String to, String subject, String body,
                             String attachmentFilename, byte[] attachmentBytes, String attachmentContentType);
}
